# Plugin manifest catalog

Five real `manifest.json` files, in the exact shape [`plugin_registry.py`](../plugin_registry.py)
scans for (`<name>/manifest.json`, one directory per plugin) — not the synthetic single-plugin
fixture the demo writes to a temp directory, an actual small catalog worth pointing the registry
at directly:

```python
from plugin_registry import PluginRegistry
registry = PluginRegistry("manifests")
for manifest in registry.list_plugins():
    print(manifest.name, manifest.requested_permissions)
```

| Plugin | Requests | What it deliberately doesn't do |
|---|---|---|
| [`calendar/`](calendar/manifest.json) | `calendar:read`, `calendar:write` | — |
| [`ticketing/`](ticketing/manifest.json) | `tickets:read`, `tickets:comment`, `tickets:transition` | Commenting and transitioning status are separate permissions — explaining itself isn't the same trust level as changing ticket state |
| [`messaging/`](messaging/manifest.json) | `messaging:post` | No read access to channel history at all — a chat plugin that posts on the agent's behalf doesn't need to read what humans said |
| [`readonly-database/`](readonly-database/manifest.json) | `database:query` | No write tool exists in the manifest — not just ungranted, never implemented |
| [`source-control/`](source-control/manifest.json) | `repo:read`, `repo:pr_write` | No merge or force-push tool, and `open_pull_request` only opens a PR from an already-pushed branch — it can't commit code itself |

## The pattern worth noticing across all five

Every one of these narrows its tool surface at the manifest level, not just at the permission
level. `readonly-database` is the clearest example: it would be easy to ship one `run_query` tool
and rely on a permission check to block writes, but a permission check is a runtime gate that can
have a bug — a manifest with no write tool at all can't have that bug, because there's nothing to
call. The same logic shows up in `source-control` not exposing a merge tool and `ticketing`
splitting `comment` from `transition` into separate permissions instead of one broad `tickets:write`.
None of this replaces the enforcement in `plugin_registry.py`'s `grant()`/`call_tool()` — it's a
second, independent layer of the same least-privilege instinct, applied at design time instead of
call time.
