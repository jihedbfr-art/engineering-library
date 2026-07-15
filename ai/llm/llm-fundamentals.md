# LLM Fundamentals

## What a large language model actually does

It predicts the next token, given the previous ones — trained on enormous text corpora, then aligned to be helpful via fine-tuning and feedback. Everything else (chat, reasoning, tools) is built on that loop.

## Vocabulary you need

| Term | Meaning |
|---|---|
| **Token** | Unit of text (~¾ of a word in English). You pay and count in tokens |
| **Context window** | Max tokens the model can consider at once (input + output) |
| **Temperature** | Randomness dial. 0 = deterministic-ish, 1+ = creative |
| **System prompt** | Standing instructions that frame every response |
| **Embedding** | Vector representing meaning — the basis of semantic search |
| **Hallucination** | Confident, plausible, wrong output |
| **Fine-tuning** | Extra training to specialize a model |
| **Inference** | Running the model to generate output |

## Calling an LLM API — universal pattern

```python
response = client.messages.create(
    model="...",
    max_tokens=1024,
    system="You are a concise technical assistant.",
    messages=[
        {"role": "user", "content": "Explain connection pooling in 3 sentences."}
    ],
)
```

Principles that transfer across all providers:
1. The API is **stateless** — you resend the conversation each turn.
2. **System prompt** = behavior contract. Put rules there, not in user turns.
3. Long context ≠ free: cost, latency and attention dilution grow with it.
4. Set `max_tokens` and handle the "ran out" case explicitly.

## Choosing an approach for a task

```
Simple task, general knowledge        → prompt engineering alone
Needs YOUR data (docs, tickets, db)   → RAG (see rag.md)
Needs a consistent style/format
 at very high volume                  → fine-tuning
Needs to DO things (APIs, files)      → tools/agents (see ../agents/)
```
Order matters: try prompting before RAG, RAG before fine-tuning. Complexity is a cost.

## Hallucinations — engineering around them

- Give the model the facts (RAG) instead of asking it to remember.
- Ask for citations to the provided sources; verify they exist.
- Constrain output (JSON schema, enums) — less room to invent.
- For critical paths: a second model call to verify, or a human gate.
- Log outputs; measure. "It seemed fine" is not an eval.

## Security: prompt injection (the #1 LLM risk)

If your app feeds untrusted text (user input, web pages, emails) to a model that has tools or secrets, that text can hijack instructions ("ignore previous instructions and..."). Defenses:
- Treat retrieved/user content as **data, never instructions** — delimit it clearly.
- Least-privilege tools; require confirmation for irreversible actions.
- Never put secrets in prompts; assume anything in context can leak into output.
- OWASP publishes a **Top 10 for LLM applications** — read it before shipping.
