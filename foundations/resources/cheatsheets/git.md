# Git Cheatsheet

## Daily driver

```bash
git status -sb                    # short status
git add -p                        # stage hunk by hunk (review yourself)
git commit -m "feat: add note archiving"
git pull --rebase                 # update without merge noise
git push
git switch -c feature/tags        # new branch
git switch main                   # back
```

## Undo — the panic table

| Situation | Command | Safe? |
|---|---|---|
| Unstage a file | `git restore --staged f` | ✅ |
| Discard local file changes | `git restore f` | ⚠️ changes gone |
| Fix last commit message | `git commit --amend` | ⚠️ not if pushed |
| Undo last commit, keep changes | `git reset --soft HEAD~1` | ✅ |
| Undo a pushed commit | `git revert <sha>` | ✅ makes a counter-commit |
| Find anything you "lost" | `git reflog` | 🛟 lifesaver |

## Inspecting

```bash
git log --oneline --graph --all     # the map
git log -p -- path/file             # history of one file with diffs
git blame -L 10,20 file             # who wrote lines 10-20
git diff main...feature             # what the branch adds
git show <sha>                      # one commit in full
```

## Branching & sync

```bash
git fetch --all --prune             # update refs, drop dead remotes
git rebase main                     # replay my branch on fresh main
git rebase -i HEAD~4                # clean up my last 4 commits (before pushing!)
git cherry-pick <sha>               # copy one commit here
git stash / git stash pop           # pocket the mess / take it back
```

## Rules of the road

1. Commit messages: imperative, specific — `fix: prevent duplicate note ids`, not `update`.
2. Never rewrite history that others have pulled (`rebase`/`amend` only on your unpushed work).
3. `.gitignore` before first commit — secrets and `node_modules` in history are forever.
4. Small commits, one intent each — future-you does the code archaeology.
5. Protected `main` + PRs, even solo: the diff review catches your own mistakes.

## Config worth setting once

```bash
git config --global init.defaultBranch main
git config --global pull.rebase true
git config --global fetch.prune true
git config --global alias.lg "log --oneline --graph --all"
```
