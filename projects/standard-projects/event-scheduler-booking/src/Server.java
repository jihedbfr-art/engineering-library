import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Point d'entree du serveur de reservation de creneaux.
 * Sert le frontend statique (public/) et expose l'API sous /api/*.
 */
public class Server {

    static final int PORT = 8080;
    static final Path DATA_FILE = Path.of("data", "slots.json");
    static final Path PUBLIC_DIR = Path.of("public");

    public static void main(String[] args) throws IOException {
        Store.load(DATA_FILE);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/slots", new SlotsHandler());
        server.createContext("/api/book", new BookHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("event-scheduler-booking demarre sur http://localhost:" + PORT);
    }

    /** GET /api/slots?date=YYYY-MM-DD -> liste des creneaux d'un jour (ou tous si pas de parametre).
     *  POST /api/slots -> creer un creneau { "date": "...", "time": "...", "capacity": 1 }
     *  DELETE /api/slots?id=... -> supprimer un creneau non reserve
     */
    static class SlotsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                String method = ex.getRequestMethod();
                if (method.equals("GET")) {
                    Map<String, String> query = HttpUtil.parseQuery(ex.getRequestURI().getRawQuery());
                    String date = query.get("date");
                    List<Object> slots = Store.listSlots(date);
                    HttpUtil.sendJson(ex, 200, slots);
                } else if (method.equals("POST")) {
                    Map<String, Object> body = Json.asObject(Json.parse(HttpUtil.readBody(ex)));
                    String date = Json.str(body, "date", null);
                    String time = Json.str(body, "time", null);
                    int capacity = (int) Json.num(body, "capacity", 1);
                    if (date == null || time == null || date.isBlank() || time.isBlank()) {
                        HttpUtil.sendJson(ex, 400, Map.of("error", "date et time sont requis"));
                        return;
                    }
                    Object created = Store.createSlot(date, time, capacity);
                    Store.save(DATA_FILE);
                    HttpUtil.sendJson(ex, 201, created);
                } else if (method.equals("DELETE")) {
                    Map<String, String> query = HttpUtil.parseQuery(ex.getRequestURI().getRawQuery());
                    String id = query.get("id");
                    if (id == null) {
                        HttpUtil.sendJson(ex, 400, Map.of("error", "id requis"));
                        return;
                    }
                    boolean removed = Store.deleteSlot(id);
                    if (!removed) {
                        HttpUtil.sendJson(ex, 404, Map.of("error", "creneau introuvable ou deja reserve"));
                        return;
                    }
                    Store.save(DATA_FILE);
                    HttpUtil.sendJson(ex, 200, Map.of("ok", true));
                } else {
                    HttpUtil.sendJson(ex, 405, Map.of("error", "methode non supportee"));
                }
            } catch (Exception e) {
                HttpUtil.sendJson(ex, 500, Map.of("error", e.getMessage() == null ? "erreur interne" : e.getMessage()));
            }
        }
    }

    /** POST /api/book -> reserver un creneau { "slotId": "...", "name": "...", "email": "..." } */
    static class BookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equals("POST")) {
                HttpUtil.sendJson(ex, 405, Map.of("error", "methode non supportee"));
                return;
            }
            try {
                Map<String, Object> body = Json.asObject(Json.parse(HttpUtil.readBody(ex)));
                String slotId = Json.str(body, "slotId", null);
                String name = Json.str(body, "name", null);
                String email = Json.str(body, "email", null);
                if (slotId == null || name == null || email == null
                        || slotId.isBlank() || name.isBlank() || email.isBlank()) {
                    HttpUtil.sendJson(ex, 400, Map.of("error", "slotId, name et email sont requis"));
                    return;
                }
                if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                    HttpUtil.sendJson(ex, 400, Map.of("error", "email invalide"));
                    return;
                }
                Store.BookResult result = Store.book(slotId, name, email);
                if (!result.ok) {
                    HttpUtil.sendJson(ex, 409, Map.of("error", result.error));
                    return;
                }
                Store.save(DATA_FILE);
                HttpUtil.sendJson(ex, 200, result.slot);
            } catch (Exception e) {
                HttpUtil.sendJson(ex, 500, Map.of("error", e.getMessage() == null ? "erreur interne" : e.getMessage()));
            }
        }
    }

    /** Sert les fichiers statiques du dossier public/. */
    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String reqPath = ex.getRequestURI().getPath();
            if (reqPath.equals("/")) reqPath = "/index.html";
            Path file = PUBLIC_DIR.resolve(reqPath.substring(1)).normalize();
            if (!file.startsWith(PUBLIC_DIR) || !Files.exists(file) || Files.isDirectory(file)) {
                byte[] notFound = "404 Not Found".getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(404, notFound.length);
                try (OutputStream os = ex.getResponseBody()) { os.write(notFound); }
                return;
            }
            String contentType = HttpUtil.guessContentType(file.toString());
            byte[] data = Files.readAllBytes(file);
            ex.getResponseHeaders().set("Content-Type", contentType);
            ex.sendResponseHeaders(200, data.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(data); }
        }
    }
}
