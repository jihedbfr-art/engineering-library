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
 * Application de sondages : création (question + options), vote, résultats en %.
 * Stockage en mémoire uniquement — redémarrage du serveur = tout est perdu (voir README).
 */
public class VotingServer {

    static final Map<Long, Poll> polls = new ConcurrentHashMap<>();
    static final AtomicLong nextId = new AtomicLong(1);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8085), 0);
        server.createContext("/polls", new PollsHandler());
        server.createContext("/vote", new VoteHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("polling-voting-app démarré sur http://localhost:8085");
    }

    static class Poll {
        long id;
        String question;
        List<String> options;
        int[] votes;

        Poll(long id, String question, List<String> options) {
            this.id = id;
            this.question = question;
            this.options = options;
            this.votes = new int[options.size()];
        }

        String toJson() {
            int total = Arrays.stream(votes).sum();
            StringBuilder optionsJson = new StringBuilder("[");
            for (int i = 0; i < options.size(); i++) {
                double pct = total == 0 ? 0 : (votes[i] * 100.0 / total);
                optionsJson.append("{\"text\":\"").append(escape(options.get(i)))
                        .append("\",\"votes\":").append(votes[i])
                        .append(",\"percent\":").append(String.format(Locale.US, "%.1f", pct))
                        .append("}");
                if (i < options.size() - 1) optionsJson.append(",");
            }
            optionsJson.append("]");
            return "{\"id\":" + id + ",\"question\":\"" + escape(question) + "\",\"total\":" + total
                    + ",\"options\":" + optionsJson + "}";
        }
    }

    static class PollsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            if (method.equals("GET") && path.equals("/polls")) {
                handleList(exchange);
            } else if (method.equals("POST") && path.equals("/polls")) {
                handleCreate(exchange);
            } else {
                respondJson(exchange, 404, "{\"error\":\"not found\"}");
            }
        }

        void handleList(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder("[");
            List<Poll> list = new ArrayList<>(polls.values());
            list.sort(Comparator.comparingLong(p -> p.id));
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i).toJson());
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            respondJson(exchange, 200, sb.toString());
        }

        void handleCreate(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String question = field(body, "question");
            List<String> options = fieldArray(body, "options");
            if (question.isBlank() || options.size() < 2) {
                respondJson(exchange, 400, "{\"error\":\"question et au moins 2 options requises\"}");
                return;
            }
            Poll poll = new Poll(nextId.getAndIncrement(), question, options);
            polls.put(poll.id, poll);
            respondJson(exchange, 201, poll.toJson());
        }
    }

    static class VoteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                respondJson(exchange, 405, "{\"error\":\"method not allowed\"}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            long pollId;
            int optionIndex;
            try {
                pollId = Long.parseLong(field(body, "pollId"));
                optionIndex = Integer.parseInt(field(body, "optionIndex"));
            } catch (NumberFormatException e) {
                respondJson(exchange, 400, "{\"error\":\"pollId/optionIndex invalides\"}");
                return;
            }
            Poll poll = polls.get(pollId);
            if (poll == null || optionIndex < 0 || optionIndex >= poll.options.size()) {
                respondJson(exchange, 404, "{\"error\":\"poll ou option introuvable\"}");
                return;
            }
            synchronized (poll) {
                poll.votes[optionIndex]++;
            }
            respondJson(exchange, 200, poll.toJson());
        }
    }

    static String field(String json, String name) {
        String key = "\"" + name + "\":";
        int idx = json.indexOf(key);
        if (idx < 0) return "";
        int start = idx + key.length();
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start < json.length() && json.charAt(start) == '"') {
            start++;
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
        } else {
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
            return json.substring(start, end);
        }
    }

    static List<String> fieldArray(String json, String name) {
        String key = "\"" + name + "\":[";
        int idx = json.indexOf(key);
        List<String> result = new ArrayList<>();
        if (idx < 0) return result;
        int start = idx + key.length();
        int end = json.indexOf(']', start);
        if (end < 0) return result;
        String content = json.substring(start, end);
        boolean inString = false;
        StringBuilder current = new StringBuilder();
        boolean escaped = false;
        for (char c : content.toCharArray()) {
            if (escaped) {
                current.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                inString = !inString;
                if (!inString) {
                    result.add(current.toString());
                    current = new StringBuilder();
                }
            } else if (inString) {
                current.append(c);
            }
        }
        return result;
    }

    static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
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
