import json
import time
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import urlparse, parse_qs

PRESENCE: dict[str, float] = {}
ONLINE_THRESHOLD_SEC = 10


class PresenceHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path.startswith("/heartbeat"):
            params = parse_qs(urlparse(self.path).query)
            user = params.get("user", ["anonymous"])[0]
            PRESENCE[user] = time.time()
            self._respond(200, {"status": "ok"})
        else:
            self._respond(404, {"error": "not found"})

    def do_GET(self):
        if self.path == "/presence":
            now = time.time()
            online = [u for u, last in PRESENCE.items() if now - last <= ONLINE_THRESHOLD_SEC]
            self._respond(200, {"online": online, "total_seen": len(PRESENCE)})
        else:
            self._respond(404, {"error": "not found"})

    def _respond(self, status, payload):
        body = json.dumps(payload).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, *args):
        pass


if __name__ == "__main__":
    print("POST /heartbeat?user=alice pour signaler une presence")
    print("GET  /presence pour voir qui est en ligne")
    HTTPServer(("localhost", 8100), PresenceHandler).serve_forever()
