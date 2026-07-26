# Recipe: schema validation gate (reject malformed tool arguments before they execute)

**Problem:** a model can hallucinate a tool call with the wrong argument types, a missing required
field, or an extra field the tool doesn't expect — and unless something checks the call against the
tool's own declared schema before it runs, that malformed call reaches real code, where it either
crashes with a confusing stack trace, or worse, gets coerced by a permissive downstream (a string
where an int was expected, silently truncated) into doing something subtly wrong. Every plugin
manifest in [`07-extensibility/manifests`](../../07-extensibility/manifests/) already declares an
`input_schema` per tool — this hook is what actually makes that declaration load-bearing instead of
documentation nobody enforces.

**Hook point:** `PRE_TOOL_USE`, high priority (before the call reaches the tool's dispatcher — a
schema violation should never even reach [duplicate-call-deduper](duplicate-call-deduper.md) or
[rate-limiter](rate-limiter.md), since a malformed call isn't a legitimate call to rate-limit).

```python
from jsonschema import Draft202012Validator
from agent_hooks import HookContext, HookDecision, HookResult

class SchemaValidationGate:
    def __init__(self, tool_schemas: dict[str, dict]):
        # tool_name -> JSON Schema, the same input_schema shape used in the
        # plugin manifests this library already ships
        self._validators = {
            name: Draft202012Validator(schema) for name, schema in tool_schemas.items()
        }

    def check(self, ctx: HookContext) -> HookResult:
        tool_name = ctx.payload.get("tool_name", "")
        validator = self._validators.get(tool_name)
        if validator is None:
            return HookResult()  # no schema registered for this tool — nothing to validate against

        arguments = ctx.payload.get("arguments", {})
        errors = sorted(validator.iter_errors(arguments), key=lambda e: e.path)
        if errors:
            first = errors[0]
            location = "/".join(str(p) for p in first.path) or "(top level)"
            return HookResult(
                decision=HookDecision.DENY,
                reason=f"argument schema violation at '{location}': {first.message}",
            )
        return HookResult()
```

**Registration, loading schemas straight from the manifest catalog:**
```python
import json
from pathlib import Path

tool_schemas = {}
for manifest_file in Path("../../07-extensibility/manifests").glob("*/manifest.json"):
    data = json.loads(manifest_file.read_text(encoding="utf-8"))
    for tool in data["tools"]:
        tool_schemas[tool["name"]] = tool["input_schema"]

gate = SchemaValidationGate(tool_schemas)
registry.register(HookPoint.PRE_TOOL_USE, gate.check, name="schema_validation_gate", priority=5)
```

**Why this belongs in the hook layer and not just in each plugin's own code:** relying on every
plugin implementation to validate its own inputs means the coverage is only as consistent as the
least careful plugin author. A single hook enforcing every registered tool's schema means a new
plugin gets real input validation for free the moment its manifest is registered, the same
one-mechanism-covers-everything argument [pii-redaction](pii-redaction.md) makes for redacting
once upstream instead of trusting every downstream consumer to redact independently.

**Failure mode to avoid:** a schema so loose it validates almost anything (every field
`additionalProperties: true`, no `required` list) gives a false sense of coverage — this hook is
only as good as the schemas it's checking against, so a schema audit is worth doing alongside
adding this hook, not instead of it.
