import argparse
import json
import pathlib
from http.server import BaseHTTPRequestHandler, HTTPServer

ROUTES_DIR = pathlib.Path(".")


class MockHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        route_file = ROUTES_DIR / (self.path.strip("/").replace("/", "_") + ".json")
        if route_file.exists():
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(route_file.read_bytes())
        else:
            self.send_response(404)
            self.end_headers()
            self.wfile.write(json.dumps({"error": "no mock for " + self.path}).encode())


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Sert des reponses JSON en dur depuis un dossier, par route")
    parser.add_argument("--dir", default=".", help="dossier contenant les fichiers <route>.json")
    parser.add_argument("--port", type=int, default=8080)
    args = parser.parse_args()
    ROUTES_DIR = pathlib.Path(args.dir)
    print(f"Mock API sur http://localhost:{args.port} (routes depuis {ROUTES_DIR})")
    HTTPServer(("localhost", args.port), MockHandler).serve_forever()
