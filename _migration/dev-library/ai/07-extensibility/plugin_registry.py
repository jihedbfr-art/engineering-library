"""A plugin/connector registry: discovers plugin manifests declaring their tools and
required permissions, and enforces that a plugin can only be called with the
permissions actually granted to it — not whatever it requested. See
plugin-architecture.md for why "requested" and "granted" have to be different sets,
not the same thing rubber-stamped.

This models the local-process style of plugin (a manifest.json next to a module that
implements the tools) rather than a networked one, to keep the demo dependency-free —
the registry/permission-enforcement logic is the same regardless of transport.

Install: pip install -r ../requirements.txt
Run:     python plugin_registry.py
"""

from __future__ import annotations

import json
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Callable

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import ConfigError, get_logger  # noqa: E402

logger = get_logger(__name__)


class PermissionDeniedError(Exception):
    """Raised when a tool call requires a permission the caller never granted to this plugin."""


@dataclass
class ToolSpec:
    name: str
    description: str
    input_schema: dict
    requires_permission: str | None = None  # None = no elevated permission needed


@dataclass
class PluginManifest:
    name: str
    description: str
    requested_permissions: list[str]
    tools: list[ToolSpec]


@dataclass
class RegisteredPlugin:
    manifest: PluginManifest
    granted_permissions: set[str] = field(default_factory=set)
    dispatcher: Callable[[str, dict], str] | None = None  # None until connected


class PluginRegistry:
    def __init__(self, plugins_dir: Path | str):
        self.plugins_dir = Path(plugins_dir)
        if not self.plugins_dir.is_dir():
            raise ConfigError(f"Plugins directory not found: {self.plugins_dir}")
        self._plugins: dict[str, RegisteredPlugin] = {}
        self._scan()

    def _scan(self) -> None:
        for manifest_file in sorted(self.plugins_dir.glob("*/manifest.json")):
            try:
                manifest = self._parse_manifest(manifest_file)
            except (KeyError, ValueError, json.JSONDecodeError) as exc:
                logger.warning("skipping malformed plugin manifest at %s: %s", manifest_file, exc)
                continue
            self._plugins[manifest.name] = RegisteredPlugin(manifest=manifest)
            logger.info(
                "discovered plugin '%s' requesting permissions: %s",
                manifest.name, manifest.requested_permissions,
            )

    @staticmethod
    def _parse_manifest(manifest_file: Path) -> PluginManifest:
        data = json.loads(manifest_file.read_text(encoding="utf-8"))
        tools = [
            ToolSpec(
                name=t["name"],
                description=t["description"],
                input_schema=t.get("input_schema", {}),
                requires_permission=t.get("requires_permission"),
            )
            for t in data["tools"]
        ]
        return PluginManifest(
            name=data["name"],
            description=data["description"],
            requested_permissions=data.get("requested_permissions", []),
            tools=tools,
        )

    def list_plugins(self) -> list[PluginManifest]:
        return [p.manifest for p in self._plugins.values()]

    def grant(self, plugin_name: str, permissions: list[str]) -> None:
        """The caller decides what to actually grant — this is deliberately NOT
        `grant_all_requested()`. Least privilege means reviewing the requested list
        and granting a subset, or all of it, as a conscious decision.
        """
        plugin = self._require_plugin(plugin_name)
        ungranted_but_requested = set(permissions) - set(plugin.manifest.requested_permissions)
        if ungranted_but_requested:
            logger.warning(
                "granting permissions %s to '%s' that weren't in its manifest — unusual, double check this is intended",
                ungranted_but_requested, plugin_name,
            )
        plugin.granted_permissions = set(permissions)
        logger.info("granted %s to plugin '%s'", permissions, plugin_name)

    def connect(self, plugin_name: str, dispatcher: Callable[[str, dict], str]) -> None:
        """Wire up the actual call mechanism — a local function for this demo, an
        RPC/HTTP client to a real service in production.
        """
        self._require_plugin(plugin_name).dispatcher = dispatcher

    def call_tool(self, plugin_name: str, tool_name: str, args: dict) -> str:
        plugin = self._require_plugin(plugin_name)
        tool = next((t for t in plugin.manifest.tools if t.name == tool_name), None)
        if tool is None:
            raise ConfigError(f"Plugin '{plugin_name}' has no tool named '{tool_name}'")

        if tool.requires_permission and tool.requires_permission not in plugin.granted_permissions:
            raise PermissionDeniedError(
                f"tool '{tool_name}' on plugin '{plugin_name}' requires permission "
                f"'{tool.requires_permission}', which was never granted "
                f"(granted: {plugin.granted_permissions or 'none'})"
            )

        if plugin.dispatcher is None:
            raise ConfigError(f"Plugin '{plugin_name}' has no connected dispatcher — call connect() first")

        logger.info("calling %s.%s(%s)", plugin_name, tool_name, args)
        return plugin.dispatcher(tool_name, args)

    def _require_plugin(self, plugin_name: str) -> RegisteredPlugin:
        plugin = self._plugins.get(plugin_name)
        if plugin is None:
            raise ConfigError(f"No plugin named '{plugin_name}'. Known: {list(self._plugins)}")
        return plugin


def _demo() -> None:
    import tempfile

    with tempfile.TemporaryDirectory() as tmp:
        plugins_root = Path(tmp)
        _write_demo_plugin(
            plugins_root / "calendar-connector",
            name="calendar-connector",
            description="Read and create events on a team calendar.",
            requested_permissions=["read:calendar", "write:calendar"],
            tools=[
                {"name": "list_events", "description": "List upcoming events", "input_schema": {}},
                {
                    "name": "create_event", "description": "Create a calendar event",
                    "input_schema": {"type": "object", "properties": {"title": {"type": "string"}}},
                    "requires_permission": "write:calendar",
                },
            ],
        )

        registry = PluginRegistry(plugins_root)
        print("Discovered plugins:")
        for m in registry.list_plugins():
            print(f"  - {m.name}: requests {m.requested_permissions}")

        # Deliberately grant less than requested — read-only, per least privilege
        registry.grant("calendar-connector", ["read:calendar"])
        registry.connect("calendar-connector", dispatcher=_fake_calendar_dispatcher)

        print("\nread-only call ->", registry.call_tool("calendar-connector", "list_events", {}))

        try:
            registry.call_tool("calendar-connector", "create_event", {"title": "Standup"})
        except PermissionDeniedError as exc:
            print(f"\nwrite call correctly denied -> {exc}")


def _fake_calendar_dispatcher(tool_name: str, args: dict) -> str:
    if tool_name == "list_events":
        return "Standup at 09:00, Architecture review at 14:00"
    return f"executed {tool_name} with {args}"


def _write_demo_plugin(directory: Path, **manifest_data: Any) -> None:
    directory.mkdir(parents=True, exist_ok=True)
    (directory / "manifest.json").write_text(json.dumps(manifest_data, indent=2), encoding="utf-8")


if __name__ == "__main__":
    _demo()
