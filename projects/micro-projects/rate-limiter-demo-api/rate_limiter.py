import time
from http.server import BaseHTTPRequestHandler, HTTPServer
from collections import defaultdict

BUCKETS: dict[str, list[float]] = defaultdict(list)
LIMIT = 5
WINDOW_SECONDS = 10


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        client = self.client_address[0]
        now = time.time()
        BUCKETS[client] = [t for t in BUCKETS[client] if now - t < WINDOW_SECONDS]

        if len(BUCKETS[client]) >= LIMIT:
            self.send_response(429)
            self.send_header("Retry-After", str(WINDOW_SECONDS))
            self.end_headers()
            self.wfile.write(b'{"error":"rate limited"}')
        else:
            BUCKETS[client].append(now)
            self.send_response(200)
            self.end_headers()
            remaining = LIMIT - len(BUCKETS[client])
            self.wfile.write(f'{{"ok":true,"remaining":{remaining}}}'.encode())


if __name__ == "__main__":
    print(f"Rate limiter demo (fixed window, {LIMIT} req / {WINDOW_SECONDS}s) sur http://localhost:8080")
    HTTPServer(("localhost", 8080), Handler).serve_forever()
