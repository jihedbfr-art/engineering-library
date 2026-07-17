# simple-blog-static-generator

Generateur de site statique minimal : lit des fichiers `.md`, les convertit en HTML, genere une
page d'index avec liens - le principe de base de Jekyll/Hugo en une cinquantaine de lignes.

## Lancer
```bash
mkdir posts && echo "# Mon premier article" > posts/hello.md
python generate.py posts dist
```
