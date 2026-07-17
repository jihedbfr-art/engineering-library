# simple-analytics-pixel-counter

Pixel de tracking analytics minimal (le principe derriere Google Analytics historique) : un GIF
transparent 1x1 servi par requete, chaque appel incremente un compteur par page.

## Lancer
```bash
javac AnalyticsPixelServer.java && java AnalyticsPixelServer
curl "http://localhost:8070/pixel.gif?page=home"
curl http://localhost:8070/stats
```
