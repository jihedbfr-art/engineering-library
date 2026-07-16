import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Chat via HTTP long-polling (PAS de vrai WebSocket — voir README pour la justification).
 * POST /send {user,message} ajoute un message. GET /messages?since=timestamp renvoie les
 * messages plus récents que `since` (timestamp epoch ms).
 */
public class ChatServer {

    static final List<Message> messages = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8084), 0);
        server.createContext("/send", new SendHandler());
        server.createContext("/messages", new MessagesHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("chat-app-websocket (long-polling) démarré sur http://localhost:8084");
    }

    static class Message {
        long timestamp;
        String user;
        String text;

        Message(long timestamp, String user, String text) {
            this.timestamp = timestamp;
            this.user = user;
            this.text = text;
        }

        String toJson() {
            return "{\"timestamp\":" + timestamp + ",\"user\":\"" + escape(user) + "\",\"message\":\"" + escape(text) + "\"}";
        }
    }

    static class SendHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                respondJson(exchange, 405, "{\"error\":\"method not allowed\"}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String user = field(body, "user");
            String text = field(body, "message");
            if (user.isBlank() || text.isBlank()) {
                respondJson(exchange, 400, "{\"error\":\"user and message required\"}");
                return;
            }
            Message m = new Message(System.currentTimeMillis(), user, text);
            messages.add(m);
            respondJson(exchange, 201, m.toJson());
        }
    }

    static class MessagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            long since = 0;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] kv = param.split("=", 2);
                    if (kv.length == 2 && kv[0].equals("since")) {
                        try {
                            since = Long.parseLong(URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            StringBuilder sb = new StringBuilder("[");
            long finalSince = since;
            List<Message> recent = messages.stream().filter(m -> m.timestamp > finalSince).toList();
            for (int i = 0; i < recent.size(); i++) {
                sb.append(recent.get(i).toJson());
                if (i < recent.size() - 1) sb.append(",");
            }
            sb.append("]");
            respondJson(exchange, 200, sb.toString());
        }
    }

    static String field(String json, String name) {
        String key = "\"" + name + "\":";
        int idx = json.indexOf(key);
        if (idx < 0) return "";
        int start = json.indexOf('"', idx + key.length()) + 1;
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                sb.append(c);
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
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
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
