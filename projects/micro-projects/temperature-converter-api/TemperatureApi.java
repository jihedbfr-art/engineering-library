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
 * API minimaliste de conversion de température.
 * Un seul fichier, JDK standard uniquement (com.sun.net.httpserver.HttpServer).
 *
 * Endpoint : GET /convert?value=100&from=C&to=F
 * Réponse  : {"input":100.0,"from":"C","to":"F","result":212.0}
 * Unités acceptées (insensibles à la casse) : C (Celsius), F (Fahrenheit), K (Kelvin).
 */
public class TemperatureApi {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/convert", new ConvertHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Serveur démarré sur http://localhost:" + port + "/convert?value=100&from=C&to=F");
    }

    static class ConvertHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Méthode non autorisée, utilisez GET\"}");
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String valueParam = params.get("value");
            String from = params.get("from");
            String to = params.get("to");

            if (valueParam == null || from == null || to == null) {
                sendJson(exchange, 400, "{\"error\":\"Paramètres 'value', 'from' et 'to' requis\"}");
                return;
            }

            double value;
            try {
                value = Double.parseDouble(valueParam);
            } catch (NumberFormatException e) {
                sendJson(exchange, 400, "{\"error\":\"'value' doit être numérique\"}");
                return;
            }

            String fromUnit = from.trim().toUpperCase(Locale.US);
            String toUnit = to.trim().toUpperCase(Locale.US);

            if (!isValidUnit(fromUnit) || !isValidUnit(toUnit)) {
                sendJson(exchange, 400, "{\"error\":\"Unités valides: C, F, K\"}");
                return;
            }

            double celsius = toCelsius(value, fromUnit);
            double result = fromCelsius(celsius, toUnit);
            double rounded = Math.round(result * 100.0) / 100.0;

            String json = String.format(Locale.US,
                    "{\"input\":%s,\"from\":%s,\"to\":%s,\"result\":%s}",
                    value, jsonString(fromUnit), jsonString(toUnit), rounded);
            sendJson(exchange, 200, json);
        }
    }

    static boolean isValidUnit(String unit) {
        return unit.equals("C") || unit.equals("F") || unit.equals("K");
    }

    static double toCelsius(double value, String unit) {
        switch (unit) {
            case "C": return value;
            case "F": return (value - 32) * 5.0 / 9.0;
            case "K": return value - 273.15;
            default: throw new IllegalArgumentException("Unité inconnue: " + unit);
        }
    }

    static double fromCelsius(double celsius, String unit) {
        switch (unit) {
            case "C": return celsius;
            case "F": return celsius * 9.0 / 5.0 + 32;
            case "K": return celsius + 273.15;
            default: throw new IllegalArgumentException("Unité inconnue: " + unit);
        }
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
                default: sb.append(c);
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
