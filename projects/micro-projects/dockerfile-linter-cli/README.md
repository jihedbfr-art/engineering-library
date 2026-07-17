# dockerfile-linter-cli

Verifie quelques bonnes pratiques Dockerfile courantes : presence d'un `USER` non-root,
`FROM ...:latest` a eviter, `ADD` utilise a la place de `COPY`.

## Lancer

```bash
python dockerfile_lint.py Dockerfile
```
