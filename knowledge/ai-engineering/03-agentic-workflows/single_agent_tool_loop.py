"""The single-agent-plus-good-tools baseline that building-agents.md argues you should
start with before reaching for multi-agent orchestration (research_agent_crew.py is the
escalation, not the default). This file is the pseudocode from that doc's "The agent
loop" section, made concrete: bounded steps, explicit tool schemas, and a HookRegistry
from 06-agent-hooks-and-skills wired in for pre-tool-use vetoing — proof that hooks and
a single-agent loop are meant to compose, not live in separate demos.

Install: pip install -r ../requirements.txt
Run:     python single_agent_tool_loop.py "What's 15% of 340, and what time is it?"
"""

from __future__ import annotations

import sys
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Callable

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import LLMCallError, get_logger, require_env, retry_with_backoff, timed  # noqa: E402
sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "06-agent-hooks-and-skills"))
from agent_hooks import HookContext, HookDecision, HookPoint, HookRegistry  # noqa: E402

logger = get_logger(__name__)

MAX_STEPS = 6  # always bound the loop — see building-agents.md's "Safety & reliability"


@dataclass
class Tool:
    name: str
    description: str
    input_schema: dict
    fn: Callable[[dict], str]


def _calculate(args: dict) -> str:
    """A deliberately narrow, safe arithmetic evaluator — never eval() untrusted
    input directly, that's a code-execution vulnerability wearing a calculator's
    clothes.
    """
    import ast
    import operator

    ops = {ast.Add: operator.add, ast.Sub: operator.sub, ast.Mult: operator.mul,
           ast.Div: operator.truediv, ast.Pow: operator.pow, ast.USub: operator.neg}

    def _eval(node):
        if isinstance(node, ast.Constant):
            return node.value
        if isinstance(node, ast.BinOp) and type(node.op) in ops:
            return ops[type(node.op)](_eval(node.left), _eval(node.right))
        if isinstance(node, ast.UnaryOp) and type(node.op) in ops:
            return ops[type(node.op)](_eval(node.operand))
        raise ValueError(f"unsupported expression: {ast.dump(node)}")

    try:
        result = _eval(ast.parse(args["expression"], mode="eval").body)
        return str(result)
    except Exception as exc:  # noqa: BLE001 - return errors as text the model can recover from
        return f"error evaluating expression: {exc}"


def _get_current_time(args: dict) -> str:
    return datetime.now(timezone.utc).isoformat()


TOOLS = [
    Tool(
        name="calculate",
        description="Evaluate a basic arithmetic expression (+, -, *, /, **). Input must be a valid expression, not a word problem.",
        input_schema={
            "type": "object",
            "properties": {"expression": {"type": "string", "description": "e.g. '340 * 0.15'"}},
            "required": ["expression"],
        },
        fn=_calculate,
    ),
    Tool(
        name="get_current_time",
        description="Get the current UTC time in ISO 8601 format.",
        input_schema={"type": "object", "properties": {}},
        fn=_get_current_time,
    ),
]


class SingleAgentLoop:
    """The loop itself stays generic — tools and hooks are what give it capability
    and constraints. Swap TOOLS for a different set and this class doesn't change.
    """

    def __init__(self, tools: list[Tool], *, hooks: HookRegistry | None = None, max_steps: int = MAX_STEPS):
        self.tools = {t.name: t for t in tools}
        self.hooks = hooks or HookRegistry()
        self.max_steps = max_steps

    def run(self, task: str) -> str:
        messages = [{"role": "user", "content": task}]

        for step in range(1, self.max_steps + 1):
            with timed(f"step {step}/{self.max_steps}", logger):
                response = self._call_llm(messages)

            if response["stop_reason"] != "tool_use":
                return response["text"]

            tool_name = response["tool_name"]
            tool_args = response["tool_args"]

            hook_result = self.hooks.run(HookContext(
                point=HookPoint.PRE_TOOL_USE,
                payload={"tool_name": tool_name, "arguments": tool_args},
                session_id="single-agent-demo",
            ))
            if hook_result.decision is HookDecision.DENY:
                tool_output = f"Tool call blocked by policy: {hook_result.reason}"
                logger.warning(tool_output)
            else:
                tool_output = self._execute_tool(tool_name, tool_args)

            messages.append({"role": "assistant", "content": response["raw"]})
            messages.append({"role": "user", "content": f"Tool result for {tool_name}: {tool_output}"})

        logger.warning("max steps (%d) reached without a final answer", self.max_steps)
        return "I wasn't able to finish within the allowed number of steps."

    def _execute_tool(self, name: str, args: dict) -> str:
        tool = self.tools.get(name)
        if tool is None:
            return f"error: unknown tool '{name}'"
        try:
            return tool.fn(args)
        except Exception as exc:  # noqa: BLE001 - errors go back to the model as text, not a crash
            logger.error("tool '%s' raised: %s", name, exc)
            return f"error executing {name}: {exc}"

    @retry_with_backoff(max_attempts=3, retryable_exceptions=(Exception,))
    def _call_llm(self, messages: list[dict]) -> dict:
        try:
            import anthropic
        except ImportError as exc:
            raise LLMCallError("anthropic SDK not installed. Run: pip install anthropic") from exc

        client = anthropic.Anthropic(api_key=require_env("ANTHROPIC_API_KEY"))
        response = client.messages.create(
            model="claude-sonnet-5",
            max_tokens=1024,
            tools=[{"name": t.name, "description": t.description, "input_schema": t.input_schema}
                   for t in self.tools.values()],
            messages=messages,
        )

        tool_use_block = next((b for b in response.content if b.type == "tool_use"), None)
        if tool_use_block:
            return {
                "stop_reason": "tool_use",
                "tool_name": tool_use_block.name,
                "tool_args": tool_use_block.input,
                "raw": [b.model_dump() for b in response.content],
            }

        text_block = next((b for b in response.content if b.type == "text"), None)
        return {"stop_reason": "end_turn", "text": text_block.text if text_block else ""}


def _demo(task: str) -> None:
    hooks = HookRegistry()

    def block_negative_amounts(ctx: Any) -> Any:
        from agent_hooks import HookResult
        if ctx.payload.get("tool_name") == "calculate" and "-" in str(ctx.payload.get("arguments", {})):
            logger.info("allowing subtraction — this hook only demonstrates the wiring, not a real rule")
        return HookResult()

    hooks.register(HookPoint.PRE_TOOL_USE, block_negative_amounts, name="demo_hook")

    agent = SingleAgentLoop(TOOLS, hooks=hooks)
    print(agent.run(task))


if __name__ == "__main__":
    task = " ".join(sys.argv[1:]) or "What's 15% of 340, and what time is it right now?"
    _demo(task)
