import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class IdempotencyStore<T> {
    private final ConcurrentMap<String, T> results = new ConcurrentHashMap<>();

    public T executeOnce(String idempotencyKey, java.util.function.Supplier<T> action) {
        return results.computeIfAbsent(idempotencyKey, k -> action.get());
    }

    public static void main(String[] args) {
        IdempotencyStore<String> store = new IdempotencyStore<>();
        String key = "payment-req-42";
        for (int i = 0; i < 3; i++) {
            String result = store.executeOnce(key, () -> {
                System.out.println("Traitement reel du paiement...");
                return "payment-id-abc123";
            });
            System.out.println("Appel " + (i + 1) + " -> " + result);
        }
    }
}
