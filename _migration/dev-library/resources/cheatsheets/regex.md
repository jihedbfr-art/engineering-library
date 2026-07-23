# Regex Cheatsheet

The tool you reach for constantly and re-learn the syntax of every single time. This page exists so you stop googling the same three things.

## Anchors & boundaries

```
^        start of string/line
$        end of string/line
\b       word boundary
\B       NOT a word boundary
```

## Character classes

```
.        any character except newline
\d       digit           [0-9]
\D       non-digit
\w       word char        [A-Za-z0-9_]
\W       non-word char
\s       whitespace       (space, tab, newline)
\S       non-whitespace
[abc]    any of a, b, c
[^abc]   NOT a, b, or c
[a-z]    range
```

## Quantifiers

```
*        0 or more
+        1 or more
?        0 or 1 (optional)
{3}      exactly 3
{2,5}    between 2 and 5
{2,}     2 or more
```
**Greedy by default** — `.*` grabs as much as possible. Add `?` to make it lazy: `.*?` grabs as little as possible. This one distinction explains 80% of "why did my regex match too much" bugs.

## Groups & alternation

```
(abc)          capturing group — can be referenced later
(?:abc)        non-capturing group — groups without creating a backreference
(?<name>abc)   named capturing group
a|b            a OR b
(?=abc)        positive lookahead — matches a position, doesn't consume
(?!abc)        negative lookahead
(?<=abc)       positive lookbehind
(?<!abc)       negative lookbehind
```

## Ready-to-use patterns (test before trusting in production)

```regex
^[\w.+-]+@[\w-]+\.[a-zA-Z]{2,}$          # email — good enough for most apps, not RFC-perfect
^\+?\d{1,3}[-.\s]?\(?\d{1,4}\)?[-.\s]?\d{1,10}$   # phone (loose, international)
^\d{4}-\d{2}-\d{2}$                       # date YYYY-MM-DD
^https?://[^\s/$.?#].[^\s]*$               # URL (basic)
^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$      # password: 8+ chars, upper, lower, digit
^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$         # hex color
```
For anything security-sensitive (email verification before sending real mail, phone validation before billing), don't trust a regex alone — regex checks *shape*, not *validity*. Verify emails by sending one; verify phones via [SMS/carrier APIs](../../telecom/camara-network-apis.md).

## Common mistakes that bite everyone eventually

```
.        matches ANY char, not literally "." — escape it: \.
+        in a URL context often needs escaping if literal
$100     the $ needs escaping in some contexts — \$100
```

## Language quick-reference

```python
# Python
import re
re.search(r'\d+', text)              # find first match
re.findall(r'\d+', text)             # find all matches
re.sub(r'\d+', 'N', text)            # replace
re.match(r'^\d+', text)              # match only at the start
```
```js
// JavaScript
/\d+/.test(text)                      // boolean check
text.match(/\d+/g)                    // find all matches (g flag)
text.replace(/\d+/g, 'N')             // replace all
```
```sql
-- PostgreSQL
SELECT * FROM users WHERE email ~ '^[\w.]+@example\.com$';
```

## Debugging a regex

Use an interactive tester (regex101.com or similar) with **explanation mode on** before shipping anything non-trivial — reading back a regex you wrote five minutes ago without help is harder than it should be, and it only gets worse six months later.
