import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * API minimaliste de citation aléatoire.
 * Un seul fichier, JDK standard uniquement (com.sun.net.httpserver.HttpServer).
 *
 * Endpoint : GET /quote
 * Réponse  : {"quote":"...","author":"..."}
 */
public class QuoteApi {

    static final List<Quote> QUOTES = buildQuotes();

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/quote", new QuoteHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Serveur démarré sur http://localhost:" + port + "/quote");
    }

    static class QuoteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Méthode non autorisée, utilisez GET\"}");
                return;
            }
            Quote q = QUOTES.get(ThreadLocalRandom.current().nextInt(QUOTES.size()));
            String json = "{\"quote\":" + jsonString(q.text) + ",\"author\":" + jsonString(q.author) + "}";
            sendJson(exchange, 200, json);
        }
    }

    static class Quote {
        final String text;
        final String author;
        Quote(String text, String author) {
            this.text = text;
            this.author = author;
        }
    }

    static List<Quote> buildQuotes() {
        List<Quote> list = new ArrayList<>();
        list.add(new Quote("Le succès, c'est d'aller d'échec en échec sans perdre son enthousiasme.", "Winston Churchill"));
        list.add(new Quote("La simplicité est la sophistication suprême.", "Léonard de Vinci"));
        list.add(new Quote("Le code est plus souvent lu qu'écrit.", "Guido van Rossum"));
        list.add(new Quote("Un programme qui fonctionne du premier coup est un programme mal testé.", "Anonyme"));
        list.add(new Quote("La perfection est atteinte, non pas lorsqu'il n'y a plus rien à ajouter, mais lorsqu'il n'y a plus rien à retirer.", "Antoine de Saint-Exupéry"));
        list.add(new Quote("Le talent gagne des matchs, mais le travail d'équipe et l'intelligence gagnent des championnats.", "Michael Jordan"));
        list.add(new Quote("Ce qui ne me tue pas me rend plus fort.", "Friedrich Nietzsche"));
        list.add(new Quote("La logique vous mènera d'un point A à un point B. L'imagination vous mènera partout.", "Albert Einstein"));
        list.add(new Quote("Il n'y a pas de vent favorable pour celui qui ne sait pas où il va.", "Sénèque"));
        list.add(new Quote("Fais de ta vie un rêve, et d'un rêve, une réalité.", "Antoine de Saint-Exupéry"));
        list.add(new Quote("Tout ce que vous avez toujours voulu se trouve de l'autre côté de la peur.", "George Addair"));
        list.add(new Quote("L'important n'est pas de convaincre, mais de donner à réfléchir.", "Bernard Werber"));
        list.add(new Quote("Deux choses sont infinies : l'univers et la bêtise humaine.", "Albert Einstein"));
        list.add(new Quote("Le meilleur moyen de prédire l'avenir, c'est de l'inventer.", "Alan Kay"));
        list.add(new Quote("Premature optimization is the root of all evil.", "Donald Knuth"));
        list.add(new Quote("Any fool can write code that a computer can understand. Good programmers write code that humans can understand.", "Martin Fowler"));
        list.add(new Quote("First, solve the problem. Then, write the code.", "John Johnson"));
        list.add(new Quote("Experience is the name everyone gives to their mistakes.", "Oscar Wilde"));
        list.add(new Quote("La vie, c'est comme une bicyclette, il faut avancer pour ne pas perdre l'équilibre.", "Albert Einstein"));
        list.add(new Quote("On ne voit bien qu'avec le cœur. L'essentiel est invisible pour les yeux.", "Antoine de Saint-Exupéry"));
        return list;
    }

    static String jsonString(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
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
