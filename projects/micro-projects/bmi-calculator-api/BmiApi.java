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
import java.util.Locale;
import java.util.Map;

/**
 * API minimaliste de calcul d'IMC (indice de masse corporelle).
 * Un seul fichier, JDK standard uniquement (com.sun.net.httpserver.HttpServer).
 *
 * Endpoint : GET /bmi?weight=70&height=1.75
 * Réponse  : {"bmi":22.86,"category":"Corpulence normale"}
 * weight en kilogrammes, height en mètres.
 */
public class BmiApi {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/bmi", new BmiHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Serveur démarré sur http://localhost:" + port + "/bmi?weight=70&height=1.75");
    }

    static class BmiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Méthode non autorisée, utilisez GET\"}");
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String weightParam = params.get("weight");
            String heightParam = params.get("height");

            if (weightParam == null || heightParam == null) {
                sendJson(exchange, 400, "{\"error\":\"Paramètres 'weight' et 'height' requis\"}");
                return;
            }

            double weight;
            double height;
            try {
                weight = Double.parseDouble(weightParam);
                height = Double.parseDouble(heightParam);
            } catch (NumberFormatException e) {
                sendJson(exchange, 400, "{\"error\":\"'weight' et 'height' doivent être numériques\"}");
                return;
            }

            if (weight <= 0 || height <= 0) {
                sendJson(exchange, 400, "{\"error\":\"'weight' et 'height' doivent être strictement positifs\"}");
                return;
            }

            double bmi = weight / (height * height);
            double rounded = Math.round(bmi * 100.0) / 100.0;
            String category = categorize(bmi);

            String json = String.format(Locale.US, "{\"bmi\":%s,\"category\":%s}", rounded, jsonString(category));
            sendJson(exchange, 200, json);
        }
    }

    static String categorize(double bmi) {
        if (bmi < 18.5) return "Insuffisance pondérale";
        if (bmi < 25.0) return "Corpulence normale";
        if (bmi < 30.0) return "Surpoids";
        return "Obésité";
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
