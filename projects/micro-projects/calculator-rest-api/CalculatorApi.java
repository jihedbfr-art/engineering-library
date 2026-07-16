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
 * API REST minimaliste de calculatrice.
 * Un seul fichier, JDK standard uniquement (com.sun.net.httpserver.HttpServer), pas de Spring Boot.
 *
 * Endpoint : GET /calculate?op=add|sub|mul|div&a=3&b=5
 * Réponse succès : {"a":3,"b":5,"op":"add","result":8}
 * Réponse erreur  : HTTP 400 + {"error":"message"}
 */
public class CalculatorApi {

    public static void main(String[] args) throws IOException {
        int port = 8081;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/calculate", new CalculateHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Serveur démarré sur http://localhost:" + port + "/calculate?op=add&a=3&b=5");
    }

    static class CalculateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Méthode non autorisée, utilisez GET\"}");
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String op = params.get("op");
            String aStr = params.get("a");
            String bStr = params.get("b");

            if (op == null || aStr == null || bStr == null) {
                sendJson(exchange, 400, "{\"error\":\"Paramètres requis: op, a, b\"}");
                return;
            }

            double a;
            double b;
            try {
                a = Double.parseDouble(aStr);
                b = Double.parseDouble(bStr);
            } catch (NumberFormatException e) {
                sendJson(exchange, 400, "{\"error\":\"a et b doivent être des nombres\"}");
                return;
            }

            double result;
            switch (op) {
                case "add":
                    result = a + b;
                    break;
                case "sub":
                    result = a - b;
                    break;
                case "mul":
                    result = a * b;
                    break;
                case "div":
                    if (b == 0) {
                        sendJson(exchange, 400, "{\"error\":\"Division par zéro impossible\"}");
                        return;
                    }
                    result = a / b;
                    break;
                default:
                    sendJson(exchange, 400, "{\"error\":\"Opération inconnue: " + op + ". Utilisez add, sub, mul ou div\"}");
                    return;
            }

            String json = String.format(
                    "{\"a\":%s,\"b\":%s,\"op\":\"%s\",\"result\":%s}",
                    formatNumber(a), formatNumber(b), op, formatNumber(result)
            );
            sendJson(exchange, 200, json);
        }
    }

    static String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
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

    static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
