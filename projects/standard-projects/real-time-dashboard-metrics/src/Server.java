import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

    static final int PORT = 8080;
    static final Path PUBLIC_DIR = Path.of("public");
    static final int MAX_HISTORY = 60;

    static final Queue<Map<String, Object>> history = new ConcurrentLinkedQueue<>();
    static final Random random = new Random();

    public static void main(String[] args) throws IOException {
        Files.createDirectories(PUBLIC_DIR);

        // Generator loop
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(Server::generateMetric, 0, 2, TimeUnit.SECONDS);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/metrics/history", new HistoryHandler());
        server.createContext("/api/metrics", new CurrentMetricHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("real-time-dashboard-metrics demarre sur http://localhost:" + PORT);
    }

    private static void generateMetric() {
        double cpu = Math.min(100.0, Math.max(5.0, 30.0 + random.nextGaussian() * 15.0));
        double memory = Math.min(8192.0, Math.max(1024.0, 4096.0 + random.nextGaussian() * 500.0));
        int rps = Math.max(10, (int) (250 + random.nextGaussian() * 80));
        double latency = Math.max(2.0, 15.0 + random.nextGaussian() * 8.0);

        Map<String, Object> point = Map.of(
                "timestamp", System.currentTimeMillis(),
                "cpu", Math.round(cpu * 100.0) / 100.0,
                "memory", Math.round(memory),
                "rps", rps,
                "latency", Math.round(latency * 10.0) / 10.0
        );

        history.add(point);
        while (history.size() > MAX_HISTORY) {
            history.poll();
        }
    }

    static class CurrentMetricHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("GET")) {
                HttpUtil.sendJson(ex, 405, Map.of("error", "methode non autorisee"));
                return;
            }
            List<Map<String, Object>> list = new ArrayList<>(history);
            Map<String, Object> latest = list.isEmpty() ? Map.of() : list.get(list.size() - 1);
            HttpUtil.sendJson(ex, 200, latest);
        }
    }

    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("GET")) {
                HttpUtil.sendJson(ex, 405, Map.of("error", "methode non autorisee"));
                return;
            }
            List<Map<String, Object>> list = new ArrayList<>(history);
            HttpUtil.sendJson(ex, 200, list);
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
