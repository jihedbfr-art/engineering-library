# sql-query-formatter-cli

Reformate une requete SQL en mettant chaque clause majeure (SELECT/FROM/WHERE/JOIN/...)
sur sa propre ligne - lisibilite rapide, pas un vrai parseur SQL.

## Lancer

```bash
javac SqlFormatter.java && java SqlFormatter "select id,name from users where active=true"
```
