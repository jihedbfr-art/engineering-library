# Skills — packaged, discoverable capability instead of one giant prompt

## The problem skills solve

A single system prompt that tries to teach a model every procedure it might need — how your
team writes commit messages, how to run your deploy checklist, how to format a specific report
— grows without bound and costs context budget on every single call, whether that procedure is
relevant to the current task or not. [Tokenization & embeddings](../01-foundations/tokenization-and-embeddings.md)
covers why that's not free: every token in a bloated system prompt is a token not available for
the actual conversation, on every single request.

The "skill" pattern (the same idea behind Claude's Agent Skills, and convergently reinvented
under other names in several agent frameworks) separates *capability* from *context budget*: a
skill is a self-contained folder — usually a short instruction file (`SKILL.md`, with YAML
frontmatter: a name and a one-line description) plus optional scripts or reference material —
that the agent discovers by scanning a directory, but whose full content only loads into context
**when the current task actually needs it**.

## How discovery works

1. At startup, the agent scans a `skills/` directory and reads only the frontmatter (name +
   description) of every `SKILL.md` — cheap, since frontmatter is a few lines, not the whole
   skill.
2. Those name/description pairs are what the agent sees at all times — a menu, not the recipes.
3. When a task matches a skill's description, the agent loads that one skill's full body into
   context. A skill about "PDF form filling" never costs a single token on a session that never
   touches a PDF.
4. Some skills bundle scripts the agent can execute directly instead of re-deriving the logic
   from a prompt every time — the instructions explain *when* and *how* to use the script, the
   script itself does the deterministic part.

This is a retrieval problem shaped exactly like [RAG](../02-rag-architectures/), just at a
coarser granularity: instead of retrieving relevant *document chunks* per question, you're
retrieving relevant *procedures* per task. The same principle from
[rag-concepts.md](../02-rag-architectures/rag-concepts.md) — don't stuff the context with
everything "to be safe" — applies here to instructions instead of documents.

## Skills vs. tools vs. RAG — they're not the same lever

| Mechanism | Answers | Cost model |
|---|---|---|
| **Tool** | "What can the agent *do*?" (an API call, a function) | A schema in every call; execution is explicit and structured |
| **Skill** | "What does the agent *know how to do*, procedurally?" | Near-zero until matched, then a full instruction block |
| **RAG** | "What *facts* does the agent need for this specific question?" | A handful of chunks per query, re-selected every time |

A tool without a skill explaining when to reach for it gets misused or ignored. A skill without
a bounded tool to actually execute the deterministic part turns into the model re-deriving
error-prone logic in prose every time. They're complementary layers, not competing choices — see
[`skill_loader.py`](skill_loader.py) for a skill that itself wraps a script, and
[`../03-agentic-workflows/building-agents.md`](../03-agentic-workflows/building-agents.md) for
the tool-design rules that still apply to whatever a skill tells the agent to call.

## What makes a good skill description

The description is the only part of a skill that's *always* in context — it has to earn a
match without the agent ever reading the body. The failure modes are symmetric:
- **Too vague** ("helps with documents") — the agent can't tell when to load it, or loads it for
  everything, defeating the point.
- **Too narrow** ("formats a Q3 revenue table for the Northeast region") — misses the 90% of
  cases that are really the same skill with different inputs.

Write the description the way you'd write a search-index summary: specific enough to match real
tasks, general enough to cover the actual range of situations the skill handles.

## See also

- [`skill_loader.py`](skill_loader.py) — a working scanner/loader implementing the two-phase
  discovery described above
- [`hooks-pattern.md`](hooks-pattern.md) — the complementary pattern for constraining *what an
  agent is allowed to do*, where skills are about *what it knows how to do*
- [`../05-evaluation-observability/`](../05-evaluation-observability/) — a skill's description
  quality is itself something worth evaling: does the agent actually load the right skill for a
  given task, measured against a held-out set of tasks, not guessed
