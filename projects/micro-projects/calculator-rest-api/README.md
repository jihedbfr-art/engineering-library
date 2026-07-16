# calculator-rest-api

API REST minimaliste en Java pur (`com.sun.net.httpserver.HttpServer`, aucune dépendance,
pas de Spring Boot) exposant les 4 opérations arithmétiques de base, avec gestion propre
de la division par zéro.

## Compiler et lancer

```bash
javac CalculatorApi.java
java CalculatorApi
```

Le serveur écoute sur `http://localhost:8081`.

## Exemple d'usage

```bash
$ curl "http://localhost:8081/calculate?op=add&a=3&b=5"
{"a":3,"b":5,"op":"add","result":8}

$ curl "http://localhost:8081/calculate?op=div&a=10&b=2"
{"a":10,"b":2,"op":"div","result":5}

$ curl "http://localhost:8081/calculate?op=div&a=10&b=0"
{"error":"Division par zéro impossible"}

$ curl "http://localhost:8081/calculate?op=mod&a=1&b=1"
{"error":"Opération inconnue: mod. Utilisez add, sub, mul ou div"}
```

Opérations supportées : `add`, `sub`, `mul`, `div`.
