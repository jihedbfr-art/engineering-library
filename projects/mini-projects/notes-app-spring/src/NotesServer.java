import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serveur de notes CRUD minimal, sans framework.
 * Stockage : fichier JSON local (notes.json), fait main (pas de lib JSON externe).
 */
public class NotesServer {

    static final Path DATA_FILE = Paths.get("notes.json");
    static final List<Note> notes = new ArrayList<>();
    static final AtomicLong nextId = new AtomicLong(1);

    public static void main(String[] args) throws IOException {
        loadNotes();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/notes", new NotesHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("notes-app-spring démarré sur http://localhost:8080");
    }

    // ---- modèle ----
    static class Note {
        long id;
        String title;
        String content;

        Note(long id, String title, String content) {
            this.id = id;
            this.title = title;
            this.content = content;
        }

        String toJson() {
            return "{\"id\":" + id + ",\"title\":\"" + escape(title) + "\",\"content\":\"" + escape(content) + "\"}";
        }
    }

    static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    // ---- persistance JSON maison ----
    static synchronized void saveNotes() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < notes.size(); i++) {
            sb.append(notes.get(i).toJson());
            if (i < notes.size() - 1) sb.append(",");
        }
        sb.append("]");
        try {
            Files.writeString(DATA_FILE, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static synchronized void loadNotes() {
        if (!Files.exists(DATA_FILE)) return;
        try {
            String json = Files.readString(DATA_FILE, StandardCharsets.UTF_8).trim();
            if (json.length() < 2) return;
            json = json.substring(1, json.length() - 1); // enlève [ ]
            long maxId = 0;
            for (String obj : splitObjects(json)) {
                Note n = parseNote(obj);
                if (n != null) {
                    notes.add(n);
                    maxId = Math.max(maxId, n.id);
                }
            }
            nextId.set(maxId + 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static List<String> splitObjects(String json) {
        List<String> result = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) result.add(json.substring(start, i + 1));
            }
        }
        return result;
    }

    static Note parseNote(String obj) {
        try {
            long id = Long.parseLong(field(obj, "id", false));
            String title = field(obj, "title", true);
            String content = field(obj, "content", true);
            return new Note(id, title, content);
        } catch (Exception e) {
            return null;
        }
    }

    static String field(String obj, String name, boolean isString) {
        String key = "\"" + name + "\":";
        int idx = obj.indexOf(key);
        if (idx < 0) return isString ? "" : "0";
        int start = idx + key.length();
        if (isString) {
            start = obj.indexOf('"', start) + 1;
            StringBuilder sb = new StringBuilder();
            boolean escaped = false;
            for (int i = start; i < obj.length(); i++) {
                char c = obj.charAt(i);
                if (escaped) {
                    if (c == 'n') sb.append('\n'); else sb.append(c);
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            int end = start;
            while (end < obj.length() && (Character.isDigit(obj.charAt(end)))) end++;
            return obj.substring(start, end);
        }
    }

    // ---- handler API /notes ----
    static class NotesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            try {
                if (method.equals("GET") && path.equals("/notes")) {
                    handleList(exchange);
                } else if (method.equals("POST") && path.equals("/notes")) {
                    handleCreate(exchange);
                } else if (method.equals("DELETE") && path.startsWith("/notes/")) {
                    handleDelete(exchange, path);
                } else {
                    respond(exchange, 404, "{\"error\":\"not found\"}");
                }
            } catch (Exception e) {
                respond(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
            }
        }

        void handleList(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder("[");
            synchronized (notes) {
                for (int i = 0; i < notes.size(); i++) {
                    sb.append(notes.get(i).toJson());
                    if (i < notes.size() - 1) sb.append(",");
                }
            }
            sb.append("]");
            respond(exchange, 200, sb.toString());
        }

        void handleCreate(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String title = field(body, "title", true);
            String content = field(body, "content", true);
            if (title.isBlank()) {
                respond(exchange, 400, "{\"error\":\"title required\"}");
                return;
            }
            Note n = new Note(nextId.getAndIncrement(), title, content);
            synchronized (notes) {
                notes.add(n);
                saveNotes();
            }
            respond(exchange, 201, n.toJson());
        }

        void handleDelete(HttpExchange exchange, String path) throws IOException {
            long id;
            try {
                id = Long.parseLong(path.substring("/notes/".length()));
            } catch (NumberFormatException e) {
                respond(exchange, 400, "{\"error\":\"bad id\"}");
                return;
            }
            synchronized (notes) {
                boolean removed = notes.removeIf(n -> n.id == id);
                if (removed) saveNotes();
            }
            respond(exchange, 204, "");
        }
    }

    // ---- handler fichiers statiques (page HTML) ----
    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            Path file = Paths.get("public" + path);
            if (!Files.exists(file)) {
                respond(exchange, 404, "not found");
                return;
            }
            byte[] bytes = Files.readAllBytes(file);
            String contentType = path.endsWith(".css") ? "text/css" :
                    path.endsWith(".js") ? "application/javascript" : "text/html";
            exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static void respond(HttpExchange exchange, int status, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
