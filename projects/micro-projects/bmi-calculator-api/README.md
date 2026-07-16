# bmi-calculator-api

API HTTP minimaliste en Java pur (`com.sun.net.httpserver.HttpServer`, aucune dépendance,
pas de Spring Boot) qui calcule l'IMC (indice de masse corporelle) à partir d'un poids
et d'une taille, et renvoie la catégorie correspondante.

## Compiler et lancer

```bash
javac BmiApi.java
java BmiApi
```

Le serveur écoute sur `http://localhost:8080`.

## Exemple d'usage

```bash
$ curl "http://localhost:8080/bmi?weight=70&height=1.75"
{"bmi":22.86,"category":"Corpulence normale"}

$ curl "http://localhost:8080/bmi?weight=50&height=1.60"
{"bmi":19.53,"category":"Corpulence normale"}

$ curl "http://localhost:8080/bmi"
{"error":"Paramètres 'weight' et 'height' requis"}
```
