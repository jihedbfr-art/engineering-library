# temperature-converter-api

API HTTP minimaliste en Java pur (`com.sun.net.httpserver.HttpServer`, aucune dépendance,
pas de Spring Boot) qui convertit une température entre Celsius, Fahrenheit et Kelvin.

## Compiler et lancer

```bash
javac TemperatureApi.java
java TemperatureApi
```

Le serveur écoute sur `http://localhost:8080`.

## Exemple d'usage

```bash
$ curl "http://localhost:8080/convert?value=100&from=C&to=F"
{"input":100.0,"from":"C","to":"F","result":212.0}

$ curl "http://localhost:8080/convert?value=0&from=C&to=K"
{"input":0.0,"from":"C","to":"K","result":273.15}

$ curl "http://localhost:8080/convert?value=32&from=F&to=C"
{"input":32.0,"from":"F","to":"C","result":0.0}
```
