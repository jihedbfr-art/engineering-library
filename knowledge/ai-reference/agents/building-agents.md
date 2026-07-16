# Building AI Agents

## What is an agent

An LLM in a loop with tools and a goal:

```
     ┌─────────────────────────────┐
     │  LLM decides next action    │◄────┐
     └──────────────┬──────────────┘     │
                    ▼                    │
        call a tool (search, code,      │
        file, API...) → observe result ──┘
                    │
                    ▼ (goal reached)
                 final answer
```

The difference with a chatbot: the model *acts*, observes results, and adapts — multiple steps without a human between each.

## Tool use — the foundation

You describe tools (name, purpose, JSON schema of parameters); the model returns a structured call; **your code executes it** and feeds the result back.

```json
{
  "name": "search_orders",
  "description": "Search customer orders by status and date range.",
  "input_schema": {
    "type": "object",
    "properties": {
      "status": {"type": "string", "enum": ["pending", "shipped", "cancelled"]},
      "since": {"type": "string", "description": "ISO date"}
    },
    "required": ["status"]
  }
}
```

Tool design rules:
1. **Descriptions are prompts** — precise wording changes behavior more than you'd think.
2. Few, well-named tools beat many overlapping ones.
3. Return errors as informative text (the model can recover); crash silently and it can't.
4. Idempotent + read-only tools by default; side-effectful ones gated.

## The agent loop (pseudocode)

```python
messages = [{"role": "user", "content": task}]
for _ in range(MAX_STEPS):                     # always bound the loop
    resp = llm(messages, tools=TOOLS)
    if resp.stop_reason == "tool_use":
        result = execute(resp.tool_call)       # your code, sandboxed
        messages += [resp, tool_result(result)]
    else:
        return resp.text                       # done
```

## Safety & reliability (non-negotiable)

- **Bound everything**: max steps, max cost, timeouts per tool.
- **Least privilege**: an agent with `run_shell` and prod credentials is an incident, not a feature.
- **Human gate** on irreversible actions (send, delete, pay, deploy).
- **Sandbox execution** (containers) for code-running agents.
- **Log every step** — you cannot debug what you didn't record.
- Treat all tool results as untrusted input (prompt injection travels through web pages, files, API responses).

## Multi-agent — when (and when not)

Start with ONE agent + good tools. Split only when:
- context per subtask is huge and independent (research fan-out),
- roles genuinely conflict (writer vs critic),
- parallelism buys real wall-clock time.

Multi-agent systems multiply cost and failure modes; orchestration is your problem, not the model's.

## Evaluating agents

- End-to-end task success rate on a fixed scenario suite (not "it worked once").
- Track: steps used, cost per task, tool-error rate, human interventions.
- Replay failures step by step — usually a bad tool description or a missing tool.
