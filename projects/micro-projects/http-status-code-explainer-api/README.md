# http-status-code-explainer-api

Mini API (com.sun.net.httpserver, zero dependance) qui explique un code de statut HTTP.

## Lancer
```bash
javac HttpStatusServer.java && java HttpStatusServer
curl "http://localhost:8080/status?code=404"
```
