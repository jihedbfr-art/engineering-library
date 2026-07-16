/**
 * Demonstration console de LRUCache : prouve l'eviction LRU sur un scenario concret.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Demo LRUCache (capacite = 3) ===\n");

        LRUCache<String, Integer> cache = new LRUCache<>(3);

        System.out.println("put(A, 1) -> ordre: MRU d'abord");
        cache.put("A", 1);
        System.out.println("  " + cache.orderSnapshot());

        System.out.println("put(B, 2)");
        cache.put("B", 2);
        System.out.println("  " + cache.orderSnapshot());

        System.out.println("put(C, 3)  -> cache plein (capacite 3)");
        cache.put("C", 3);
        System.out.println("  " + cache.orderSnapshot());

        System.out.println("\nget(A) -> " + cache.get("A") + "  (A redevient le plus recemment utilise)");
        System.out.println("  " + cache.orderSnapshot());

        System.out.println("\nput(D, 4) -> cache plein, doit evincer le LRU actuel (B)");
        cache.put("D", 4);
        System.out.println("  " + cache.orderSnapshot());

        System.out.println("\nVerification: B a ete evince -> get(B) = " + cache.get("B") + " (attendu: null)");
        System.out.println("A est toujours present -> get(A) = " + cache.get("A") + " (attendu: 1)");
        System.out.println("C est toujours present -> get(C) = " + cache.get("C") + " (attendu: 3)");
        System.out.println("D est toujours present -> get(D) = " + cache.get("D") + " (attendu: 4)");

        boolean success = cache.get("B") == null
                && Integer.valueOf(1).equals(cache.get("A"))
                && Integer.valueOf(3).equals(cache.get("C"))
                && Integer.valueOf(4).equals(cache.get("D"));

        System.out.println("\n=== Resultat: " + (success ? "SUCCES - eviction LRU correcte" : "ECHEC") + " ===");
    }
}
