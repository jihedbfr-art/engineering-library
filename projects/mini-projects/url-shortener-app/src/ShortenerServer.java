import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Raccourcisseur d'URL avec encodage base62 de l'id incrémental.
 * POST /shorten {url} -> {code}, GET /{code} -> redirection 302 vers l'URL d'origine.
 */
public class ShortenerServer {

    static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final Map<String, String> codeToUrl = new ConcurrentHashMap<>();
    static final AtomicLong counter = new AtomicLong(1);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8082), 0);
        server.createContext("/shorten", new ShortenHandler());
        server.createContext("/", new RootHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("url-shortener-app démarré sur http://localhost:8082");
    }

    static String toBase62(long id) {
        if (id == 0) return String.valueOf(ALPHABET.charAt(0));
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(ALPHABET.charAt((int) (id % 62)));
            id /= 62;
        }
        return sb.reverse().toString();
    }

    static class ShortenHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                respondJson(exchange, 405, "{\"error\":\"method not allowed\"}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String url = extractField(body, "url");
            if (url.isBlank() || !(url.startsWith("http://") || url.startsWith("https://"))) {
                respondJson(exchange, 400, "{\"error\":\"url invalide (http:// ou https:// requis)\"}");
                return;
            }
            String code = toBase62(counter.getAndIncrement());
            codeToUrl.put(code, url);
            respondJson(exchange, 201, "{\"code\":\"" + code + "\",\"url\":\"" + escape(url) + "\"}");
        }
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                serveIndex(exchange);
                return;
            }
            String code = path.substring(1);
            String target = codeToUrl.get(code);
            if (target == null) {
                respondText(exchange, 404, "Code inconnu : " + code);
                return;
            }
            exchange.getResponseHeaders().set("Location", target);
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }

        void serveIndex(HttpExchange exchange) throws IOException {
            Path file = Paths.get("public/index.html");
            byte[] bytes = Files.readAllBytes(file);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static String extractField(String json, String name) {
        String key = "\"" + name + "\":";
        int idx = json.indexOf(key);
        if (idx < 0) return "";
        int start = json.indexOf('"', idx + key.length()) + 1;
        int end = json.indexOf('"', start);
        if (start < 0 || end < 0) return "";
        return json.substring(start, end);
    }

    static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
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
