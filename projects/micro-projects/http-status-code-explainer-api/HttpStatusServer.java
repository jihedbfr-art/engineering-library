import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.Map;

public class HttpStatusServer {
    static final Map<Integer, String> CODES = Map.ofEntries(
        Map.entry(200, "OK - requete reussie"),
        Map.entry(201, "Created - ressource creee"),
        Map.entry(204, "No Content"),
        Map.entry(301, "Moved Permanently"),
        Map.entry(304, "Not Modified"),
        Map.entry(400, "Bad Request"),
        Map.entry(401, "Unauthorized"),
        Map.entry(403, "Forbidden"),
        Map.entry(404, "Not Found"),
        Map.entry(409, "Conflict"),
        Map.entry(429, "Too Many Requests"),
        Map.entry(500, "Internal Server Error"),
        Map.entry(503, "Service Unavailable")
    );

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/status", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            int code = query != null && query.startsWith("code=") ? Integer.parseInt(query.substring(5)) : 200;
            String body = CODES.getOrDefault(code, "Code inconnu dans ce mini-annuaire");
            exchange.sendResponseHeaders(200, body.getBytes().length);
            exchange.getResponseBody().write(body.getBytes());
            exchange.close();
        });
        server.start();
        System.out.println("GET http://localhost:8080/status?code=404");
    }
}
