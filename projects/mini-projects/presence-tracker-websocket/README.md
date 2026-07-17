# presence-tracker-websocket

Suivi de presence en ligne (qui est connecte, "typing...", derniere activite) via un canal
d'evenements. Ici implemente en Python avec `socketserver` + polling long, sans dependance
websocket externe, pour illustrer le principe sans lib tierce.

## Lancer
```bash
python presence_server.py
```
Puis GET http://localhost:8100/presence pour voir qui est present.
