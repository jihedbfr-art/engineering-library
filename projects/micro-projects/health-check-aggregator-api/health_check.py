from http.server import BaseHTTPRequestHandler, HTTPServer
import json

DEPENDENCIES = {
    "database": lambda: True,
    "cache": lambda: True,
    "message-queue": lambda: True,
}


class HealthHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path != "/health":
            self.send_response(404)
            self.end_headers()
            return
        checks = {name: check() for name, check in DEPENDENCIES.items()}
        overall = "UP" if all(checks.values()) else "DOWN"
        body = json.dumps({"status": overall, "checks": checks}).encode()
        self.send_response(200 if overall == "UP" else 503)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(body)


if __name__ == "__main__":
    print("GET http://localhost:8000/health")
    HTTPServer(("localhost", 8000), HealthHandler).serve_forever()
