"""Two-agent collaborative pipeline built on CrewAI: a Researcher gathers and verifies
facts on a topic, a Writer turns them into a structured brief. The point of this file
isn't "look, agents" — it's the handoff contract between them, the tool boundary, and
the bounds (max steps, max RPM) that keep a multi-agent run from spiraling in cost or
time. See ../building-agents.md for why single-agent-plus-good-tools should be your
starting point and multi-agent is a deliberate escalation, not a default.

Uses DuckDuckGo search (no API key required) instead of a paid search API, so this
runs end-to-end for anyone who clones the repo — swap in SerperDevTool or Tavily for
production use, they're materially better search quality.

Install: pip install -r ../requirements.txt
Run:     python research_agent_crew.py "impact of edge computing on telecom BSS platforms"
"""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import LLMCallError, get_logger, require_env, retry_with_backoff, timed  # noqa: E402

logger = get_logger(__name__)

MAX_RESEARCH_STEPS = 8       # bound the researcher's tool-call loop — see building-agents.md
MAX_REQUESTS_PER_MINUTE = 10  # crude cost guard shared across the crew


def _build_search_tool():
    """A CrewAI-compatible tool wrapping DuckDuckGo's free search API. Kept as a
    function rather than a module-level import so the crewai/langchain-community
    imports only happen if this file is actually run, not on every import of the
    module (matters if another script just wants to reuse a helper from here).
    """
    from crewai.tools import BaseTool
    from pydantic import BaseModel, Field

    class SearchInput(BaseModel):
        query: str = Field(..., description="The search query")

    class DuckDuckGoSearchTool(BaseTool):
        name: str = "web_search"
        description: str = (
            "Search the web for current information on a topic. "
            "Input should be a focused query, not a full question."
        )
        args_schema: type[BaseModel] = SearchInput

        def _run(self, query: str) -> str:
            try:
                from duckduckgo_search import DDGS
            except ImportError as exc:
                raise LLMCallError(
                    "duckduckgo-search not installed. Run: pip install duckduckgo-search"
                ) from exc

            with DDGS() as ddgs:
                results = list(ddgs.text(query, max_results=5))

            if not results:
                return f"No results found for '{query}'."

            return "\n\n".join(
                f"{r['title']}\n{r['body']}\nSource: {r['href']}" for r in results
            )

    return DuckDuckGoSearchTool()


def build_crew(topic: str):
    """Assemble the researcher/writer crew for a given topic. Split out from
    run_research() so tests can inspect agent/task wiring without paying for an
    actual LLM call.
    """
    from crewai import Agent, Crew, Process, Task

    search_tool = _build_search_tool()

    researcher = Agent(
        role="Senior Research Analyst",
        goal=f"Find accurate, well-sourced, current facts about: {topic}",
        backstory=(
            "You are a meticulous analyst who cross-checks claims before reporting "
            "them. You favor recent, credible sources and flag anything you couldn't "
            "verify instead of guessing."
        ),
        tools=[search_tool],
        max_iter=MAX_RESEARCH_STEPS,
        allow_delegation=False,
        verbose=True,
    )

    writer = Agent(
        role="Technical Writer",
        goal="Turn verified research into a clear, structured brief for engineering leadership",
        backstory=(
            "You write for senior engineers and architects who want substance, not "
            "marketing language. You cite what the research found and explicitly call "
            "out open questions rather than papering over gaps."
        ),
        tools=[],
        allow_delegation=False,
        verbose=True,
    )

    research_task = Task(
        description=(
            f"Research the topic: '{topic}'. Find at least 3 distinct, credible "
            "sources. Note points of agreement and disagreement across sources. "
            "Flag anything that could not be verified."
        ),
        expected_output=(
            "A structured set of findings with source attribution for each claim."
        ),
        agent=researcher,
    )

    writing_task = Task(
        description=(
            "Using only the research findings provided, write a brief (400-600 words) "
            "aimed at engineering leadership. Structure: context, key findings with "
            "citations, and a closing section on open questions or risks. Do not "
            "introduce claims that weren't in the research."
        ),
        expected_output="A markdown-formatted brief, ready to share internally.",
        agent=writer,
        context=[research_task],
    )

    return Crew(
        agents=[researcher, writer],
        tasks=[research_task, writing_task],
        process=Process.sequential,
        max_rpm=MAX_REQUESTS_PER_MINUTE,
        verbose=True,
    )


@retry_with_backoff(max_attempts=2, retryable_exceptions=(Exception,))
def _kickoff(crew) -> str:
    result = crew.kickoff()
    return str(result)


def run_research(topic: str) -> str:
    require_env("OPENAI_API_KEY")  # CrewAI's default LLM backend; swap for ANTHROPIC_API_KEY + a custom LLM config
    logger.info("starting research crew for topic: %s", topic)

    crew = build_crew(topic)
    with timed(f"crew run for '{topic}'", logger):
        output = _kickoff(crew)

    logger.info("crew run complete, %d chars of output", len(output))
    return output


def main() -> None:
    topic = " ".join(sys.argv[1:]) or "the current state of local LLM inference with Ollama"
    try:
        brief = run_research(topic)
    except LLMCallError as exc:
        logger.error("crew run failed: %s", exc)
        sys.exit(1)

    print("\n" + "=" * 80)
    print(brief)
    print("=" * 80)


if __name__ == "__main__":
    main()
