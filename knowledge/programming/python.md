# Python

Readable, batteries-included, dominant in scripting, data, and AI. Optimizes for developer speed.

## The idioms that make you fluent

```python
# Comprehensions — the Pythonic loop
squares = [x*x for x in range(10)]
evens   = [x for x in nums if x % 2 == 0]
lookup  = {u.id: u for u in users}
uniq    = {x for x in items}

# Unpacking
a, b = b, a                       # swap
first, *rest = [1, 2, 3, 4]       # first=1, rest=[2,3,4]

# enumerate / zip instead of index juggling
for i, item in enumerate(items): ...
for name, age in zip(names, ages): ...

# f-strings
print(f"{name} is {age} years old ({age*365} days)")

# Truthiness & defaults
name = user_name or "anonymous"
value = data.get("key", default)  # no KeyError
```

## Idiomatic error handling — EAFP

"Easier to Ask Forgiveness than Permission" — try it, catch if it fails:
```python
try:
    return cache[key]
except KeyError:
    return compute(key)
```

## Structure & tooling (2020s baseline)

```bash
python -m venv .venv && source .venv/bin/activate   # isolated env
pip install -r requirements.txt
# or the modern all-in-one:
uv add requests          # uv — fast package/venv manager
ruff check . && ruff format .   # lint + format, extremely fast
pytest                   # testing
mypy .                   # static type checking
```

## Type hints — use them

```python
def greet(name: str, times: int = 1) -> str:
    return f"Hello {name}! " * times

from dataclasses import dataclass
@dataclass
class User:
    id: int
    name: str
    active: bool = True
```
Hints don't run at runtime but make code self-documenting and catch bugs via mypy/IDE.

## Gotchas that bite everyone

- **Mutable default args**: `def f(items=[])` — the list is shared across calls! Use `def f(items=None): items = items or []`.
- **Late binding in closures/loops** — lambdas in a loop capture the variable, not its value.
- **`is` vs `==`** — `is` compares identity, `==` compares value. Use `==` for values, `is` only for `None`.
- **Integer/float division**: `/` is float, `//` is floor.

## Where Python shines / doesn't

✅ Data science, ML/AI, automation, scripting, web backends (Django/FastAPI), glue code.
⚠️ CPU-bound parallelism (the GIL limits threads — use multiprocessing or native extensions), mobile, raw speed.
