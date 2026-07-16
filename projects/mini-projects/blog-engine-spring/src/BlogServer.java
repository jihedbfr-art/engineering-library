import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Moteur de blog minimal : lit des fichiers Markdown dans posts/ et les rend en HTML.
 * Pas de framework, pas de base de données. Conversion Markdown -> HTML maison (sous-ensemble simple).
 */
public class BlogServer {

    static final Path POSTS_DIR = Paths.get("posts");

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8083), 0);
        server.createContext("/", new BlogHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("blog-engine-spring démarré sur http://localhost:8083");
    }

    static class BlogHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            try {
                if (path.equals("/")) {
                    serveIndex(exchange);
                } else if (path.startsWith("/post/")) {
                    servePost(exchange, path.substring("/post/".length()));
                } else {
                    respond(exchange, 404, "<h1>404</h1>");
                }
            } catch (Exception e) {
                respond(exchange, 500, "<h1>Erreur serveur</h1><pre>" + e.getMessage() + "</pre>");
            }
        }

        void serveIndex(HttpExchange exchange) throws IOException {
            List<Path> files = listPosts();
            StringBuilder body = new StringBuilder();
            body.append("<h1>Blog</h1><ul>");
            for (Path f : files) {
                String name = f.getFileName().toString().replace(".md", "");
                String title = extractTitle(f);
                body.append("<li><a href=\"/post/")
                    .append(URLEncoder.encode(name, StandardCharsets.UTF_8))
                    .append("\">").append(escapeHtml(title)).append("</a></li>");
            }
            body.append("</ul>");
            respond(exchange, 200, page(body.toString()));
        }

        void servePost(HttpExchange exchange, String slug) throws IOException {
            Path file = POSTS_DIR.resolve(slug + ".md");
            if (!Files.exists(file)) {
                respond(exchange, 404, page("<h1>Article introuvable</h1><p><a href=\"/\">Retour</a></p>"));
                return;
            }
            String markdown = Files.readString(file, StandardCharsets.UTF_8);
            String html = MarkdownConverter.toHtml(markdown);
            respond(exchange, 200, page(html + "<p><a href=\"/\">&larr; Retour à la liste</a></p>"));
        }

        List<Path> listPosts() throws IOException {
            if (!Files.exists(POSTS_DIR)) return List.of();
            try (Stream<Path> stream = Files.list(POSTS_DIR)) {
                return stream.filter(p -> p.toString().endsWith(".md"))
                        .sorted()
                        .collect(Collectors.toList());
            }
        }

        String extractTitle(Path file) {
            try {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for (String line : lines) {
                    if (line.startsWith("# ")) return line.substring(2).trim();
                }
            } catch (IOException ignored) {
            }
            return file.getFileName().toString();
        }
    }

    static String page(String body) {
        return "<!DOCTYPE html><html lang=\"fr\"><head><meta charset=\"UTF-8\"><title>Blog</title>"
                + "<style>body{font-family:system-ui,Arial,sans-serif;max-width:700px;margin:40px auto;"
                + "padding:0 16px;line-height:1.6;color:#222;}a{color:#2563eb;}"
                + "code{background:#f4f5f7;padding:2px 4px;border-radius:3px;}</style></head><body>"
                + body + "</body></html>";
    }

    static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    static void respond(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
