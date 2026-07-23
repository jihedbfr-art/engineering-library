# Bash Cheatsheet

## Variables & basics

```bash
name="Ada"
echo "Hello, $name"           # or ${name}
readonly PI=3.14               # constant
unset name                     # remove a variable

# Command substitution
files=$(ls *.txt)
count=$(find . -name "*.log" | wc -l)
```

## Conditionals

```bash
if [ -f "$file" ]; then echo "exists"; fi
if [[ "$str" == *"needle"* ]]; then echo "found"; fi   # [[ ]] supports pattern matching
if [ "$a" -eq "$b" ]; then echo "equal"; fi             # numeric comparison

# Test flags worth memorizing
[ -f file ]   # regular file exists
[ -d dir ]    # directory exists
[ -z "$s" ]   # string is empty
[ -n "$s" ]   # string is non-empty
[ -x file ]   # file is executable
```

## Loops

```bash
for f in *.csv; do echo "$f"; done
for i in {1..5}; do echo "$i"; done
while read -r line; do echo "$line"; done < file.txt

# Loop over command output safely (handles spaces in filenames)
find . -name "*.log" -print0 | while IFS= read -r -d '' f; do echo "$f"; done
```

## Functions

```bash
greet() {
  local name=$1              # always 'local' inside functions
  echo "Hello, $name"
  return 0
}
greet "Ada"
```

## Arguments & exit codes

```bash
echo "Script: $0, first arg: $1, all args: $@, count: $#"
command; echo "exit code: $?"     # $? = last command's exit status
set -euo pipefail                  # fail fast: error on unset vars, pipe failures, non-zero exits
```
`set -euo pipefail` at the top of every script you write. It turns silent partial failures into a script that stops the moment something's wrong — the single highest-value line in bash scripting.

## Pipes & redirection

```bash
cmd1 | cmd2                  # pipe stdout of cmd1 into cmd2
cmd > file                   # stdout to file (overwrite)
cmd >> file                  # stdout to file (append)
cmd 2> errors.log            # stderr to file
cmd > out.log 2>&1           # both stdout and stderr to the same file
cmd < input.txt              # read stdin from file
```

## String manipulation

```bash
"${var:0:3}"        # substring (first 3 chars)
"${var/old/new}"     # replace first match
"${var//old/new}"    # replace all matches
"${var^^}"           # uppercase
"${var,,}"           # lowercase
"${#var}"             # length
"${var:-default}"    # use default if var is unset/empty
```

## Arrays

```bash
arr=(one two three)
echo "${arr[0]}"          # first element
echo "${arr[@]}"          # all elements
echo "${#arr[@]}"         # length
for item in "${arr[@]}"; do echo "$item"; done
```

## Common one-liners

```bash
# Find and act
find . -name "*.tmp" -delete
find . -type f -mtime +30 -exec rm {} \;      # files older than 30 days

# Process substitution — feed a command's output as if it were a file
diff <(sort a.txt) <(sort b.txt)

# Retry a flaky command
until curl -sf https://api.example.com/health; do sleep 2; done

# Run a command in the background, keep the PID
long_task.sh & pid=$!
wait $pid
```

## Debugging a script

```bash
bash -x script.sh          # print every command before running it
set -x                     # turn tracing on mid-script
set +x                     # turn it back off
trap 'echo "failed at line $LINENO"' ERR
```
