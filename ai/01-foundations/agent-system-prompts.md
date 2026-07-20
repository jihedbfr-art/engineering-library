# System Prompts for Agents — Different Rules Than a Chat Prompt

[prompt-engineering/patterns.md](prompt-engineering/patterns.md) covers prompting in general.
Once a model is driving a tool-use loop rather than just answering a question, the system prompt
has to do more work, and a few of the general patterns actively need adjusting. This file is
specifically about that gap.

## Why an agent's system prompt is a different problem

A chat system prompt sets tone and constraints for a single reply. An agent's system prompt has
to hold up across a whole sequence of autonomous decisions — which tool to call, when to stop,
when to ask for clarification versus guessing — without a human steering each individual step.
Get it vague and the agent either does too little (asks for confirmation on everything, useless)
or too much (takes irreversible actions it was never clearly authorized for).

## The four things an agent system prompt has to specify that a chat prompt doesn't

1. **The goal, not just the persona.** "You are a helpful research assistant" tells the model how
   to sound, not what "done" looks like. An agent needs an explicit success condition: what
   output or state change means the task is complete, so it can actually stop instead of
   continuing to poke at tools indefinitely.

2. **Tool-use judgment, not just tool descriptions.** The tool schema (see
   [`../03-agentic-workflows/building-agents.md`](../03-agentic-workflows/building-agents.md))
   tells the model *what a tool does*; the system prompt needs to tell it *when to prefer one
   tool over another*, and when to not use a tool at all — a model with a `search_web` tool and
   no guidance will sometimes search for something it already knows confidently, wasting a step
   and introducing a source of noise into its own context.

3. **An explicit authorization boundary for irreversible actions.** "You may read files freely;
   you must ask for explicit confirmation before deleting, sending, or publishing anything" is a
   sentence that changes real-world outcomes, not a nicety. Don't rely on the model inferring
   which actions are reversible — state it, the same way [hooks-pattern.md](../06-agent-hooks-and-skills/hooks-pattern.md)'s
   veto layer enforces it structurally as a second line of defense, not the only one.

4. **What to do when it's stuck**, explicitly — retry with a different approach, report the
   blocker and stop, or escalate to a human. Without this, a stuck agent's default behavior is
   frequently to keep trying variations of the same failed approach until it exhausts its step
   budget, burning cost without surfacing the actual problem to anyone who could unblock it.

## A structure that holds up

```
[Role and goal]
You are an agent that <does X> to accomplish <Y>. Success means <concrete end state>.

[Tools available and when to use them]
You have access to: <tool 1> — use when <condition>. <tool 2> — use when <condition>.
Do not call a tool when you already have enough information to answer directly.

[Authorization boundaries]
You may <freely-permitted actions> without asking.
You must ask for explicit confirmation before: <irreversible actions>.

[Stopping conditions]
Stop and report back once <success condition> is met.
If you cannot make progress after <N> attempts at an approach, stop and explain what's blocking
you rather than continuing to retry.

[Output contract]
Your final response must <format/structure requirement, if the caller needs a parseable output
rather than free text>.
```

## Patterns from general prompting that need adjusting for agents

- **"Think step by step" still applies, but bound it.** Extended reasoning before a tool call
  is valuable; extended reasoning that never converges to a decision just burns the step/cost
  budget from [cost-tracker.md](../06-agent-hooks-and-skills/hooks/cost-tracker.md) without
  producing an action.
- **Few-shot examples for agents should show the *tool-use sequence*, not just input→output.** A
  worked example of "given this task, call tool A, observe the result, then call tool B" teaches
  the decision pattern in a way a plain input/output pair can't.
- **"Be concise" needs a carve-out for tool-call reasoning.** Cutting the model's stated reasoning
  before a consequential tool call to save tokens can remove exactly the trace that would let a
  human debug why the agent made a bad call — see the audit-logger recipe in
  [06-agent-hooks-and-skills/hooks/](../06-agent-hooks-and-skills/hooks/audit-logger.md) for
  capturing that trace outside the response instead of cutting it from the reasoning itself.

## See also

- [prompt-engineering/patterns.md](prompt-engineering/patterns.md) — the general patterns this
  file assumes and builds on
- [`../03-agentic-workflows/building-agents.md`](../03-agentic-workflows/building-agents.md) —
  the loop this system prompt drives
- [`../06-agent-hooks-and-skills/hooks-pattern.md`](../06-agent-hooks-and-skills/hooks-pattern.md) —
  the structural enforcement layer for authorization boundaries a system prompt alone can't
  guarantee; a prompt instruction is a strong suggestion, a hook is an actual gate
