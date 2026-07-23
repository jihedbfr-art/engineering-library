"""Cross-cutting helpers shared by every module in this library: logging, retries,
error types and a couple of small conveniences that every LLM-calling script ends up
reimplementing badly if it doesn't import them from one place.
"""

from __future__ import annotations

import functools
import logging
import os
import random
import sys
import time
from contextlib import contextmanager
from typing import Any, Callable, Iterator, TypeVar

T = TypeVar("T")


class LLMCallError(Exception):
    """Raised when a call to a model provider fails after all retries are exhausted."""


class RetrievalError(Exception):
    """Raised when a retrieval backend (vector store, keyword index) can't be reached or queried."""


class ConfigError(Exception):
    """Raised when required configuration (env vars, files) is missing or invalid."""


def get_logger(name: str, level: str | None = None) -> logging.Logger:
    """Return a configured logger. Level defaults to $LOG_LEVEL or INFO.

    Every script in this library should call this instead of `logging.getLogger`
    directly, so log format stays consistent whether you're running advanced_rag.py,
    research_agent_crew.py or local_ollama_chat.py in the same terminal session.
    """
    logger = logging.getLogger(name)
    if logger.handlers:
        return logger

    resolved_level = (level or os.environ.get("LOG_LEVEL", "INFO")).upper()
    handler = logging.StreamHandler(sys.stderr)
    handler.setFormatter(
        logging.Formatter(
            fmt="%(asctime)s | %(levelname)-7s | %(name)s | %(message)s",
            datefmt="%H:%M:%S",
        )
    )
    logger.addHandler(handler)
    logger.setLevel(resolved_level)
    logger.propagate = False
    return logger


def require_env(name: str, *, default: str | None = None) -> str:
    """Fetch an env var or raise ConfigError with an actionable message.

    Prefer this over `os.environ[name]` — a bare KeyError three frames deep inside
    an SDK call tells a user nothing about which variable to set.
    """
    value = os.environ.get(name, default)
    if value is None or value == "":
        raise ConfigError(
            f"Missing required environment variable '{name}'. "
            f"Set it in your shell or a .env file before running this script."
        )
    return value


def estimate_tokens(text: str) -> int:
    """Rough token count without pulling in a tokenizer dependency.

    Uses tiktoken when it's installed (accurate for OpenAI-family models), falls back
    to the ~4-chars-per-token heuristic from 01-foundations/tokenization-and-embeddings.md
    otherwise. Good enough for budgeting and truncation guards, not for billing.
    """
    try:
        import tiktoken

        encoding = tiktoken.get_encoding("cl100k_base")
        return len(encoding.encode(text))
    except ImportError:
        return max(1, len(text) // 4)


def retry_with_backoff(
    *,
    max_attempts: int = 3,
    base_delay_seconds: float = 1.0,
    max_delay_seconds: float = 20.0,
    retryable_exceptions: tuple[type[Exception], ...] = (Exception,),
) -> Callable[[Callable[..., T]], Callable[..., T]]:
    """Retry a flaky call (rate limits, transient network errors) with exponential
    backoff and jitter. Re-raises the last exception, wrapped in LLMCallError, once
    attempts are exhausted — callers should catch LLMCallError, not the raw provider
    exception, so swapping providers later doesn't ripple through call sites.
    """

    def decorator(func: Callable[..., T]) -> Callable[..., T]:
        @functools.wraps(func)
        def wrapper(*args: Any, **kwargs: Any) -> T:
            logger = get_logger(func.__module__)
            last_exc: Exception | None = None
            for attempt in range(1, max_attempts + 1):
                try:
                    return func(*args, **kwargs)
                except retryable_exceptions as exc:  # noqa: BLE001 - intentionally broad, narrowed by caller
                    last_exc = exc
                    if attempt == max_attempts:
                        break
                    delay = min(max_delay_seconds, base_delay_seconds * (2 ** (attempt - 1)))
                    delay += random.uniform(0, delay * 0.25)  # jitter avoids retry storms
                    logger.warning(
                        "%s failed (attempt %d/%d): %s — retrying in %.1fs",
                        func.__name__,
                        attempt,
                        max_attempts,
                        exc,
                        delay,
                    )
                    time.sleep(delay)
            raise LLMCallError(
                f"{func.__name__} failed after {max_attempts} attempts: {last_exc}"
            ) from last_exc

        return wrapper

    return decorator


@contextmanager
def timed(operation: str, logger: logging.Logger | None = None) -> Iterator[None]:
    """Context manager that logs how long a block took. Cheap observability that
    costs nothing to add and saves real debugging time once latency gets weird.
    """
    log = logger or get_logger("timing")
    start = time.perf_counter()
    try:
        yield
    finally:
        elapsed_ms = (time.perf_counter() - start) * 1000
        log.info("%s took %.1fms", operation, elapsed_ms)
