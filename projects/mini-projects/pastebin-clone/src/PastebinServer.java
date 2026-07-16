import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.*;

/**
 * Clone minimal de pastebin : POST /paste crée un paste, GET /paste/{id} le récupère.
 * Stockage : un fichier texte par paste dans le dossier pastes/.
 */
public class PastebinServer {

    static final Path PASTES_DIR = Paths.get("pastes");
    static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    static final SecureRandom RANDOM = new SecureRandom();

    public static void main(String[] args) throws IOException {
        Files.createDirectories(PASTES_DIR);

        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/paste", new PasteHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("pastebin-clone démarré sur http://localhost:8081");
    }

    static String generateId() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return sb.toString();
    }

    static class PasteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            try {
                if (method.equals("POST") && path.equals("/paste")) {
                    handleCreate(exchange);
                } else if (method.equals("GET") && path.startsWith("/paste/")) {
                    handleGet(exchange, path);
                } else {
                    respondJson(exchange, 404, "{\"error\":\"not found\"}");
                }
            } catch (Exception e) {
                respondJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        void handleCreate(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String text = extractField(body, "text");
            if (text.isBlank()) {
                respondJson(exchange, 400, "{\"error\":\"text required\"}");
                return;
            }
            String id;
            Path file;
            do {
                id = generateId();
                file = PASTES_DIR.resolve(id + ".txt");
            } while (Files.exists(file));
            Files.writeString(file, text, StandardCharsets.UTF_8);
            respondJson(exchange, 201, "{\"id\":\"" + id + "\"}");
        }

        void handleGet(HttpExchange exchange, String path) throws IOException {
            String id = path.substring("/paste/".length());
            Path file = PASTES_DIR.resolve(id + ".txt");
            if (!Files.exists(file) || !id.matches("[a-zA-Z0-9]+")) {
                respondJson(exchange, 404, "{\"error\":\"paste not found\"}");
                return;
            }
            String text = Files.readString(file, StandardCharsets.UTF_8);
            respondJson(exchange, 200, "{\"id\":\"" + id + "\",\"text\":\"" + escape(text) + "\"}");
        }
    }

    static String extractField(String json, String name) {
        String key = "\"" + name + "\":";
        int idx = json.indexOf(key);
        if (idx < 0) return "";
        int start = json.indexOf('"', idx + key.length()) + 1;
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
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
    }

    static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            Path file = Paths.get("public" + path);
            if (!Files.exists(file)) {
                respondText(exchange, 404, "not found");
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

    static void respondJson(HttpExchange exchange, int status, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    static void respondText(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
