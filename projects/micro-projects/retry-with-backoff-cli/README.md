# Retry with backoff: decide before repeating

[Version française](README.fr.md)

This small Python demo retries a function with exponentially increasing delays and a random addition. Use it to study a retry loop, then examine the situations where repeating the call can make things worse. It uses the standard library and makes no network request.

## Run the existing demo

From the repository root:

```bash
cd projects/micro-projects/retry-with-backoff-cli
python retry_backoff.py
```

The synthetic function fails twice with ConnectionError, then returns succes. Expect two retry messages and a final succes line. Delays vary: about 0.5–0.6 seconds, then 1.0–1.1 seconds. Five attempts means one initial call and at most four additional calls.

## Exercise: a timeout after a write

A missing response does not tell the caller whether a write happened. Predict the number of entries before running this local experiment from the same directory:

```python
from contextlib import redirect_stdout
from io import StringIO
from unittest.mock import patch
from retry_backoff import retry_with_backoff

entries = []

def write_then_lose_response():
    entries.append("synthetic-order")
    raise TimeoutError("synthetic response lost")

with patch("retry_backoff.time.sleep"), redirect_stdout(StringIO()):
    try:
        retry_with_backoff(write_then_lose_response, max_attempts=2)
    except TimeoutError:
        pass

assert entries == ["synthetic-order", "synthetic-order"]
print(len(entries))
```

Save the snippet as an untracked local exercise file and run it with Python, or paste it into a Python session. It prints 2. The two writes are simulated by list appends; no order is sent anywhere. Mocking sleep removes the waiting, not the repeated effects. This exercise demonstrates a hazard in the current loop.

## Choose the next action

| Observation | Decision to justify | Evidence needed |
|---|---|---|
| Read request loses its connection | Consider another attempt within a time budget | Operation contract, remaining deadline, retry ownership |
| Order creation times out after sending | Determine the outcome before another write, or use documented deduplication | Stable operation identity and server contract |
| Input validation fails | Correct the request | Validation error details safe for the caller |
| Access is denied | Resolve permissions through the expected process | Authorization decision; another identical call does not grant rights |
| Server asks the client to wait | Apply the documented delay if the remaining budget permits | Retry-After, when relevant, and service-specific policy |
| Several service layers retry | Assign retry ownership and bound total work | Attempt limits at each layer |

For discussion: if three layers each make three total attempts, the deepest operation may be invoked 27 times for one caller action. This is an arithmetic upper bound for that setup, not a measurement of this script.

## What the current implementation omits

The loop catches every Exception, so it also retries permanent failures. It has no per-call timeout, overall deadline, delay cap, Retry-After handling or idempotency store. A function that never returns also prevents the loop from progressing.

Its jitter adds only 0–0.1 seconds to the exponential delay; it is not full jitter over the entire backoff window. It prints exception text, which can contain sensitive data in a real integration. Use synthetic messages here. max_attempts and base_delay are not validated: zero attempts silently returns None.

Before adapting this code, define retryable failures, validate configuration, bound time and attempts, and decide how duplicate effects are prevented. An idempotency key needs a server-side contract covering its scope, retention, concurrent calls and changed payloads. Adding a random header alone is insufficient.

## Review exercise

Write a policy for a read operation and an order creation operation. For each, specify the retry owner, stop conditions, timeout ambiguity, and observable outcome without logging credentials or request bodies. Explain which guarantees come from the client and which require the server.

## Sources

- [RFC 9110, section 9.2.2](https://www.rfc-editor.org/rfc/rfc9110.html#section-9.2.2): HTTP method idempotency and limits on automatically retrying non-idempotent requests.
- [AWS: control and limit retry calls](https://docs.aws.amazon.com/wellarchitected/latest/framework/rel_mitigate_interaction_failure_limit_retries.html): retry limits, backoff and avoiding duplicate effects.
- [AWS: making retries safe with idempotent APIs](https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/): service contracts for repeated requests.

Sources consulted on 15 October 2026. The exercise characterizes the demo; it does not establish production safety.
