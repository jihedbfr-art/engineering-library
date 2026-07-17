import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class AnalyticsPixelServer {
    static final Map<String, AtomicLong> HITS = new ConcurrentHashMap<>();
    static final byte[] TRANSPARENT_GIF = {
        0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01, 0x00, 0x01, 0x00,
        (byte) 0x80, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x00,
        0x21, (byte) 0xF9, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00,
        0x2C, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x02, 0x02, 0x44, 0x01, 0x00, 0x3B
    };

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8070), 0);

        server.createContext("/pixel.gif", exchange -> {
            String page = exchange.getRequestURI().getQuery();
            String pageKey = page != null ? page : "unknown";
            HITS.computeIfAbsent(pageKey, k -> new AtomicLong()).incrementAndGet();

            exchange.getResponseHeaders().add("Content-Type", "image/gif");
            exchange.sendResponseHeaders(200, TRANSPARENT_GIF.length);
            exchange.getResponseBody().write(TRANSPARENT_GIF);
            exchange.close();
        });

        server.createContext("/stats", exchange -> {
            StringBuilder sb = new StringBuilder();
            HITS.forEach((page, count) -> sb.append(page).append(": ").append(count).append("\n"));
            byte[] body = sb.toString().getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        server.start();
        System.out.println("Pixel de tracking : http://localhost:8070/pixel.gif?page=home");
        System.out.println("Stats             : http://localhost:8070/stats");
    }
}
