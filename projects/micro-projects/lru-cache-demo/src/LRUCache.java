import java.util.HashMap;
import java.util.Map;

/**
 * Cache LRU (Least Recently Used) implemente a la main avec une HashMap (acces O(1))
 * et une liste doublement chainee (ordre d'utilisation, O(1) pour deplacer/retirer un noeud).
 * Aucune dependance a LinkedHashMap : la logique d'eviction est explicite.
 *
 * @param <K> type de la cle
 * @param <V> type de la valeur
 */
public class LRUCache<K, V> {

    /** Noeud de la liste doublement chainee, contient la paire cle/valeur. */
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final Map<K, Node<K, V>> map;
    // sentinelles pour eviter les cas particuliers en tete/queue de liste
    private final Node<K, V> head; // cote le plus recemment utilise
    private final Node<K, V> tail; // cote le moins recemment utilise

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacite doit etre > 0");
        }
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    /** Retourne la valeur associee a la cle, ou null si absente. Marque la cle comme recemment utilisee. */
    public V get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null) {
            return null;
        }
        moveToFront(node);
        return node.value;
    }

    /** Insere ou met a jour une cle. Si la capacite est depassee, evince l'element le moins recemment utilise. */
    public void put(K key, V value) {
        Node<K, V> existing = map.get(key);
        if (existing != null) {
            existing.value = value;
            moveToFront(existing);
            return;
        }

        if (map.size() >= capacity) {
            evictLeastRecentlyUsed();
        }

        Node<K, V> node = new Node<>(key, value);
        map.put(key, node);
        addToFront(node);
    }

    public int size() {
        return map.size();
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    private void evictLeastRecentlyUsed() {
        Node<K, V> lru = tail.prev;
        if (lru == head) {
            return; // cache vide
        }
        removeNode(lru);
        map.remove(lru.key);
    }

    private void moveToFront(Node<K, V> node) {
        removeNode(node);
        addToFront(node);
    }

    private void addToFront(Node<K, V> node) {
        Node<K, V> firstReal = head.next;
        head.next = node;
        node.prev = head;
        node.next = firstReal;
        firstReal.prev = node;
    }

    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    /** Retourne les cles de la plus recemment utilisee a la moins recemment utilisee (pour debug/demo). */
    public String orderSnapshot() {
        StringBuilder sb = new StringBuilder("[");
        Node<K, V> current = head.next;
        boolean first = true;
        while (current != tail) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(current.key);
            first = false;
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
