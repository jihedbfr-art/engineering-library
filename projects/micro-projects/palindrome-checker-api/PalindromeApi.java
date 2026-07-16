import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * API minimaliste de vérification de palindrome.
 * Un seul fichier, JDK standard uniquement (com.sun.net.httpserver.HttpServer).
 *
 * Endpoint : GET /check?text=xxx
 * Réponse  : {"input":"xxx","isPalindrome":true|false}
 * Comparaison insensible à la casse, aux espaces et à la ponctuation.
 */
public class PalindromeApi {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/check", new CheckHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Serveur démarré sur http://localhost:" + port + "/check?text=xxx");
    }

    static class CheckHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Méthode non autorisée, utilisez GET\"}");
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String text = params.get("text");

            if (text == null) {
                sendJson(exchange, 400, "{\"error\":\"Paramètre 'text' manquant\"}");
                return;
            }

            boolean isPalindrome = checkPalindrome(text);
            String json = "{\"input\":" + jsonString(text) + ",\"isPalindrome\":" + isPalindrome + "}";
            sendJson(exchange, 200, json);
        }
    }

    static boolean checkPalindrome(String text) {
        StringBuilder cleaned = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                cleaned.append(Character.toLowerCase(c));
            }
        }
        String s = cleaned.toString();
        String reversed = cleaned.reverse().toString();
        return s.equals(reversed);
    }

    static Map<String, String> parseQuery(URI uri) {
        Map<String, String> result = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isEmpty()) {
            return result;
        }
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx < 0) {
                continue;
            }
            String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            result.put(key, value);
        }
        return result;
    }

    static String jsonString(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
