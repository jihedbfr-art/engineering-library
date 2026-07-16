import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Micro-API HTTP (sans framework) exposant GET /validate?number=xxx qui applique
 * l'algorithme de Luhn et renvoie {"number":"...","valid":true/false}.
 *
 * AVERTISSEMENT: ceci est un validateur de FORMAT uniquement, a but educatif.
 * Aucun numero recu n'est stocke ni logge (voir handle() : pas de System.out/println du
 * parametre "number", aucune ecriture disque). Ce n'est PAS un outil de verification de
 * cartes bancaires reelles.
 */
public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/validate", new ValidateHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("luhn-validator-api demarre sur http://localhost:" + PORT);
        System.out.println("Exemple: http://localhost:" + PORT + "/validate?number=79927398713");
        System.out.println("AVERTISSEMENT: validateur de FORMAT uniquement (algorithme de Luhn), usage educatif.");
        System.out.println("Aucun numero recu n'est stocke ni logge par ce serveur.");
    }

    static class ValidateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Methode non supportee, utiliser GET\"}");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            String number = params.get("number");

            // IMPORTANT: ne jamais logger/afficher "number" (donnee potentiellement sensible en entree).
            if (number == null || number.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"parametre 'number' manquant\"}");
                return;
            }

            boolean valid = LuhnAlgorithm.isValid(number);
            String json = String.format(
                    "{\"number\":\"%s\",\"valid\":%s}",
                    escapeJson(number), valid
            );
            sendResponse(exchange, 200, json);
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> result = new java.util.HashMap<>();
            if (query == null || query.isEmpty()) {
                return result;
            }
            for (String pair : query.split("&")) {
                int idx = pair.indexOf('=');
                if (idx > 0) {
                    String key = URIDecode(pair.substring(0, idx));
                    String value = URIDecode(pair.substring(idx + 1));
                    result.put(key, value);
                }
            }
            return result;
        }

        private String URIDecode(String s) {
            try {
                return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
            } catch (Exception e) {
                return s;
            }
        }

        private String escapeJson(String s) {
            return s.replace("\\", "\\\\").replace("\"", "\\\"");
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
