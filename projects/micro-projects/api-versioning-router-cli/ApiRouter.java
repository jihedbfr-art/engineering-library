import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ApiRouter {
    private final Map<String, Supplier<String>> handlers = new HashMap<>();

    public void register(String version, String path, Supplier<String> handler) {
        handlers.put(version + ":" + path, handler);
    }

    public String route(String version, String path) {
        Supplier<String> handler = handlers.get(version + ":" + path);
        return handler != null ? handler.get() : "404 - version/route inconnue";
    }

    public static void main(String[] args) {
        ApiRouter router = new ApiRouter();
        router.register("v1", "/users", () -> "{\"users\": [\"legacy format\"]}");
        router.register("v2", "/users", () -> "{\"data\": {\"users\": [\"nouveau format enveloppe\"]}}");

        System.out.println(router.route("v1", "/users"));
        System.out.println(router.route("v2", "/users"));
        System.out.println(router.route("v3", "/users"));
    }
}
