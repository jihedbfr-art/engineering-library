import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IdempotencyServer {
    static final Map<String, String> SEEN = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/orders", exchange -> {
            String key = exchange.getRequestHeaders().getFirst("Idempotency-Key");
            String response;
            if (key == null) {
                response = "{\"error\":\"Idempotency-Key header requis\"}";
                exchange.sendResponseHeaders(400, response.getBytes().length);
            } else if (SEEN.containsKey(key)) {
                response = SEEN.get(key);
                exchange.sendResponseHeaders(200, response.getBytes().length);
            } else {
                String orderId = "order-" + System.nanoTime();
                response = "{\"orderId\":\"" + orderId + "\",\"replayed\":false}";
                SEEN.put(key, response.replace("false", "true"));
                exchange.sendResponseHeaders(201, response.getBytes().length);
            }
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });
        server.start();
        System.out.println("Idempotency demo sur http://localhost:8080/orders (header Idempotency-Key)");
    }
}
