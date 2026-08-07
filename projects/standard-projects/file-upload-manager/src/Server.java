import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Server {

    static final int PORT = 8080;
    static final Path STORAGE_DIR = Path.of("storage");
    static final Path PUBLIC_DIR = Path.of("public");
    static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    static final Set<String> DISALLOWED_EXTENSIONS = Set.of("exe", "bat", "sh", "cmd", "ps1", "vbs", "dll");

    public static void main(String[] args) throws IOException {
        Files.createDirectories(STORAGE_DIR);
        Files.createDirectories(PUBLIC_DIR);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/upload", new UploadHandler());
        server.createContext("/api/files", new FilesHandler());
        server.createContext("/api/download", new DownloadHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("file-upload-manager demarre sur http://localhost:" + PORT);
    }

    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) {
                HttpUtil.sendJson(ex, 405, Map.of("error", "methode non autorisee"));
                return;
            }
            try {
                String contentType = ex.getRequestHeaders().getFirst("Content-Type");
                String boundary = Multipart.extractBoundary(contentType);
                if (boundary == null) {
                    HttpUtil.sendJson(ex, 400, Map.of("error", "Content-Type multipart/form-data avec boundary requis"));
                    return;
                }

                byte[] body = HttpUtil.readBody(ex);
                if (body.length > MAX_FILE_SIZE) {
                    HttpUtil.sendJson(ex, 400, Map.of("error", "Taille de fichier depasse la limite autorisee (10 Mo)"));
                    return;
                }

                List<Multipart.Part> parts = Multipart.parse(body, boundary);
                Multipart.Part filePart = null;
                for (Multipart.Part p : parts) {
                    if (p.isFile() && p.content != null && p.content.length > 0) {
                        filePart = p;
                        break;
                    }
                }

                if (filePart == null) {
                    HttpUtil.sendJson(ex, 400, Map.of("error", "Aucun fichier detecte dans le formulaire"));
                    return;
                }

                String rawFileName = filePart.fileName();
                String fileName = Path.of(rawFileName).getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
                if (fileName.isBlank()) fileName = "uploaded_file_" + System.currentTimeMillis();

                int dotIdx = fileName.lastIndexOf('.');
                if (dotIdx != -1) {
                    String ext = fileName.substring(dotIdx + 1).toLowerCase();
                    if (DISALLOWED_EXTENSIONS.contains(ext)) {
                        HttpUtil.sendJson(ex, 400, Map.of("error", "Extension non autorisee: ." + ext));
                        return;
                    }
                }

                Path targetFile = STORAGE_DIR.resolve(fileName);
                Files.write(targetFile, filePart.content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                HttpUtil.sendJson(ex, 201, Map.of(
                        "ok", true,
                        "name", fileName,
                        "size", filePart.content.length
                ));
            } catch (Exception e) {
                HttpUtil.sendJson(ex, 500, Map.of("error", e.getMessage() == null ? "erreur serveur" : e.getMessage()));
            }
        }
    }

    static class FilesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String method = ex.getRequestMethod();
            if (method.equalsIgnoreCase("GET")) {
                List<Map<String, Object>> list = new ArrayList<>();
                if (Files.exists(STORAGE_DIR)) {
                    try (var stream = Files.list(STORAGE_DIR)) {
                        stream.filter(Files::isRegularFile).forEach(p -> {
                            try {
                                list.add(Map.of(
                                        "name", p.getFileName().toString(),
                                        "size", Files.size(p),
                                        "modified", Files.getLastModifiedTime(p).toMillis()
                                ));
                            } catch (IOException ignored) {}
                        });
                    }
                }
                HttpUtil.sendJson(ex, 200, list);
            } else if (method.equalsIgnoreCase("DELETE")) {
                Map<String, String> query = HttpUtil.parseQuery(ex.getRequestURI().getRawQuery());
                String name = query.get("name");
                if (name == null || name.isBlank()) {
                    HttpUtil.sendJson(ex, 400, Map.of("error", "Nom du fichier requis"));
                    return;
                }
                Path target = STORAGE_DIR.resolve(Path.of(name).getFileName().toString());
                if (!Files.exists(target)) {
                    HttpUtil.sendJson(ex, 404, Map.of("error", "Fichier non trouve"));
                    return;
                }
                Files.delete(target);
                HttpUtil.sendJson(ex, 200, Map.of("ok", true));
            } else {
                HttpUtil.sendJson(ex, 405, Map.of("error", "Methode non autorisee"));
            }
        }
    }

    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("GET")) {
                HttpUtil.sendJson(ex, 405, Map.of("error", "Methode non autorisee"));
                return;
            }
            Map<String, String> query = HttpUtil.parseQuery(ex.getRequestURI().getRawQuery());
            String name = query.get("name");
            if (name == null || name.isBlank()) {
                HttpUtil.sendJson(ex, 400, Map.of("error", "Nom du fichier requis"));
                return;
            }
            Path file = STORAGE_DIR.resolve(Path.of(name).getFileName().toString()).normalize();
            if (!file.startsWith(STORAGE_DIR) || !Files.exists(file) || Files.isDirectory(file)) {
                HttpUtil.sendJson(ex, 404, Map.of("error", "Fichier introuvable"));
                return;
            }

            byte[] data = Files.readAllBytes(file);
            ex.getResponseHeaders().set("Content-Type", HttpUtil.guessContentType(file.toString()));
            ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + file.getFileName().toString() + "\"");
            ex.sendResponseHeaders(200, data.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(data);
            }
        }
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String reqPath = ex.getRequestURI().getPath();
            if (reqPath.equals("/")) reqPath = "/index.html";
            Path file = PUBLIC_DIR.resolve(reqPath.substring(1)).normalize();
            if (!file.startsWith(PUBLIC_DIR) || !Files.exists(file) || Files.isDirectory(file)) {
                byte[] notFound = "404 Not Found".getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(404, notFound.length);
                try (OutputStream os = ex.getResponseBody()) { os.write(notFound); }
                return;
            }
            String contentType = HttpUtil.guessContentType(file.toString());
            byte[] data = Files.readAllBytes(file);
            ex.getResponseHeaders().set("Content-Type", contentType);
            ex.sendResponseHeaders(200, data.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(data); }
        }
    }
}
