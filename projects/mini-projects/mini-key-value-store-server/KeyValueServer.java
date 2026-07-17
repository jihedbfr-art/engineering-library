import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class KeyValueServer {
    static final Map<String, String> STORE = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8090), 0);

        server.createContext("/kv/", exchange -> {
            String key = exchange.getRequestURI().getPath().substring("/kv/".length());
            String method = exchange.getRequestMethod();
            String response;
            int status;

            switch (method) {
                case "GET" -> {
                    response = STORE.getOrDefault(key, "");
                    status = STORE.containsKey(key) ? 200 : 404;
                }
                case "PUT" -> {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    STORE.put(key, body);
                    response = "OK";
                    status = 200;
                }
                case "DELETE" -> {
                    STORE.remove(key);
                    response = "OK";
                    status = 200;
                }
                default -> {
                    response = "Methode non supportee";
                    status = 405;
                }
            }
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        server.start();
        System.out.println("Store clé-valeur sur http://localhost:8090/kv/<cle>  (GET/PUT/DELETE)");
    }
}
