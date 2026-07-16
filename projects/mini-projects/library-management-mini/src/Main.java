import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Library Management Mini
 * Serveur HTTP minimal (com.sun.net.httpserver.HttpServer) — pas de Spring Boot.
 * CRUD de livres (titre, auteur, ISBN, disponible/emprunté), stockage JSON fichier local.
 *
 * Lancer :  javac -d out src/Main.java  &&  java -cp out Main
 * Puis ouvrir http://localhost:8080/
 */
public class Main {

    static final Path DATA_FILE = Path.of("books.json");
    static final List<Book> books = new ArrayList<>();
    static int nextId = 1;

    public static void main(String[] args) throws IOException {
        loadBooks();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/books", new BooksHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Library Management Mini demarre sur http://localhost:8080/");
    }

    // ---------- Modele ----------

    static class Book {
        int id;
        String title;
        String author;
        String isbn;
        boolean available;

        Book(int id, String title, String author, String isbn, boolean available) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.isbn = isbn;
            this.available = available;
        }

        String toJson() {
            return "{"
                    + "\"id\":" + id + ","
                    + "\"title\":" + Json.quote(title) + ","
                    + "\"author\":" + Json.quote(author) + ","
                    + "\"isbn\":" + Json.quote(isbn) + ","
                    + "\"available\":" + available
                    + "}";
        }
    }

    // ---------- Persistance ----------

    static synchronized void loadBooks() throws IOException {
        books.clear();
        if (!Files.exists(DATA_FILE)) {
            // Quelques livres d'exemple pour ne pas partir d'une bibliotheque vide
            books.add(new Book(nextId++, "Clean Code", "Robert C. Martin", "9780132350884", true));
            books.add(new Book(nextId++, "Effective Java", "Joshua Bloch", "9780134685991", true));
            books.add(new Book(nextId++, "1984", "George Orwell", "9780451524935", false));
            saveBooks();
            return;
        }
        String content = Files.readString(DATA_FILE, StandardCharsets.UTF_8);
        List<Map<String, String>> raw = Json.parseArray(content);
        for (Map<String, String> o : raw) {
            int id = Integer.parseInt(o.get("id"));
            Book b = new Book(id, o.get("title"), o.get("author"), o.get("isbn"),
                    Boolean.parseBoolean(o.get("available")));
            books.add(b);
            if (id >= nextId) nextId = id + 1;
        }
    }

    static synchronized void saveBooks() throws IOException {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < books.size(); i++) {
            sb.append("  ").append(books.get(i).toJson());
            if (i < books.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");
        Files.writeString(DATA_FILE, sb.toString(), StandardCharsets.UTF_8);
    }

    // ---------- Handlers ----------

    static class BooksHandler implements HttpHandler {
        // Chemins geres : /api/books  et  /api/books/{id}
        static final Pattern ID_PATH = Pattern.compile("^/api/books/(\\d+)$");

        @Override
        public void handle(HttpExchange ex) throws IOException {
            String method = ex.getRequestMethod();
            String path = ex.getRequestURI().getPath();
            try {
                Matcher m = ID_PATH.matcher(path);
                if (path.equals("/api/books")) {
                    if (method.equals("GET")) {
                        listBooks(ex);
                    } else if (method.equals("POST")) {
                        createBook(ex);
                    } else {
                        respond(ex, 405, "{\"error\":\"methode non supportee\"}");
                    }
                } else if (m.matches()) {
                    int id = Integer.parseInt(m.group(1));
                    if (method.equals("GET")) {
                        getBook(ex, id);
                    } else if (method.equals("PUT")) {
                        updateBook(ex, id);
                    } else if (method.equals("DELETE")) {
                        deleteBook(ex, id);
                    } else {
                        respond(ex, 405, "{\"error\":\"methode non supportee\"}");
                    }
                } else {
                    respond(ex, 404, "{\"error\":\"route inconnue\"}");
                }
            } catch (Exception e) {
                respond(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        void listBooks(HttpExchange ex) throws IOException {
            StringBuilder sb = new StringBuilder("[");
            synchronized (books) {
                for (int i = 0; i < books.size(); i++) {
                    sb.append(books.get(i).toJson());
                    if (i < books.size() - 1) sb.append(",");
                }
            }
            sb.append("]");
            respond(ex, 200, sb.toString());
        }

        void getBook(HttpExchange ex, int id) throws IOException {
            Optional<Book> b = findBook(id);
            if (b.isEmpty()) {
                respond(ex, 404, "{\"error\":\"livre introuvable\"}");
                return;
            }
            respond(ex, 200, b.get().toJson());
        }

        void createBook(HttpExchange ex) throws IOException {
            Map<String, String> body = Json.parseObject(readBody(ex));
            String title = body.getOrDefault("title", "").trim();
            String author = body.getOrDefault("author", "").trim();
            String isbn = body.getOrDefault("isbn", "").trim();
            if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
                respond(ex, 400, "{\"error\":\"title, author et isbn sont requis\"}");
                return;
            }
            Book b;
            synchronized (books) {
                b = new Book(nextId++, title, author, isbn, true);
                books.add(b);
                saveBooks();
            }
            respond(ex, 201, b.toJson());
        }

        void updateBook(HttpExchange ex, int id) throws IOException {
            Optional<Book> maybe = findBook(id);
            if (maybe.isEmpty()) {
                respond(ex, 404, "{\"error\":\"livre introuvable\"}");
                return;
            }
            Map<String, String> body = Json.parseObject(readBody(ex));
            Book b = maybe.get();
            synchronized (books) {
                if (body.containsKey("title")) b.title = body.get("title");
                if (body.containsKey("author")) b.author = body.get("author");
                if (body.containsKey("isbn")) b.isbn = body.get("isbn");
                if (body.containsKey("available")) b.available = Boolean.parseBoolean(body.get("available"));
                saveBooks();
            }
            respond(ex, 200, b.toJson());
        }

        void deleteBook(HttpExchange ex, int id) throws IOException {
            synchronized (books) {
                boolean removed = books.removeIf(b -> b.id == id);
                if (!removed) {
                    respond(ex, 404, "{\"error\":\"livre introuvable\"}");
                    return;
                }
                saveBooks();
            }
            respond(ex, 204, "");
        }

        Optional<Book> findBook(int id) {
            synchronized (books) {
                return books.stream().filter(b -> b.id == id).findFirst();
            }
        }
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/") || path.equals("/index.html")) {
                Path file = Path.of("public/index.html");
                if (Files.exists(file)) {
                    byte[] data = Files.readAllBytes(file);
                    ex.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
                    ex.sendResponseHeaders(200, data.length);
                    try (OutputStream os = ex.getResponseBody()) { os.write(data); }
                    return;
                }
            }
            respond(ex, 404, "{\"error\":\"non trouve\"}");
        }
    }

    // ---------- Utilitaires HTTP ----------

    static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static void respond(HttpExchange ex, int status, String json) throws IOException {
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, data.length == 0 ? -1 : data.length);
        if (data.length > 0) {
            try (OutputStream os = ex.getResponseBody()) { os.write(data); }
        }
    }

    // ---------- Mini-JSON maison (zero dependance) ----------
    // Suffisant pour des objets plats { "cle": "valeur" | booleen | nombre }.

    static class Json {
        static String quote(String s) {
            StringBuilder sb = new StringBuilder("\"");
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '"': sb.append("\\\""); break;
                    case '\\': sb.append("\\\\"); break;
                    case '\n': sb.append("\\n"); break;
                    default: sb.append(c);
                }
            }
            return sb.append("\"").toString();
        }

        static Map<String, String> parseObject(String json) {
            Map<String, String> map = new LinkedHashMap<>();
            if (json == null) return map;
            json = json.trim();
            if (json.isEmpty()) return map;
            if (json.startsWith("{")) json = json.substring(1);
            if (json.endsWith("}")) json = json.substring(0, json.length() - 1);

            List<String> pairs = splitTopLevel(json);
            for (String pair : pairs) {
                int colon = findColon(pair);
                if (colon < 0) continue;
                String key = stripQuotes(pair.substring(0, colon).trim());
                String value = pair.substring(colon + 1).trim();
                value = stripQuotes(value);
                map.put(key, value);
            }
            return map;
        }

        static List<Map<String, String>> parseArray(String json) {
            List<Map<String, String>> result = new ArrayList<>();
            json = json.trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]")) json = json.substring(0, json.length() - 1);
            for (String obj : splitTopLevelObjects(json)) {
                result.add(parseObject(obj));
            }
            return result;
        }

        static List<String> splitTopLevel(String s) {
            List<String> parts = new ArrayList<>();
            int depth = 0;
            boolean inQuotes = false;
            StringBuilder cur = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) inQuotes = !inQuotes;
                if (!inQuotes) {
                    if (c == '{' || c == '[') depth++;
                    if (c == '}' || c == ']') depth--;
                    if (c == ',' && depth == 0) {
                        parts.add(cur.toString());
                        cur = new StringBuilder();
                        continue;
                    }
                }
                cur.append(c);
            }
            if (!cur.toString().trim().isEmpty()) parts.add(cur.toString());
            return parts;
        }

        static List<String> splitTopLevelObjects(String s) {
            List<String> objects = new ArrayList<>();
            int depth = 0;
            boolean inQuotes = false;
            StringBuilder cur = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) inQuotes = !inQuotes;
                if (!inQuotes) {
                    if (c == '{') depth++;
                    if (c == '}') depth--;
                }
                cur.append(c);
                if (!inQuotes && depth == 0 && c == '}') {
                    objects.add(cur.toString());
                    cur = new StringBuilder();
                }
            }
            return objects;
        }

        static int findColon(String pair) {
            boolean inQuotes = false;
            for (int i = 0; i < pair.length(); i++) {
                char c = pair.charAt(i);
                if (c == '"' && (i == 0 || pair.charAt(i - 1) != '\\')) inQuotes = !inQuotes;
                if (c == ':' && !inQuotes) return i;
            }
            return -1;
        }

        static String stripQuotes(String s) {
            s = s.trim();
            if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
                s = s.substring(1, s.length() - 1);
                s = s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n");
            }
            return s;
        }
    }
}
