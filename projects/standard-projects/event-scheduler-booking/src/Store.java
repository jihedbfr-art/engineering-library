import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Stockage des creneaux en memoire, persiste dans un fichier JSON.
 * Toutes les operations passent par un verrou unique : le volume est
 * faible (usage mono-instance) donc pas besoin d'une structure plus fine,
 * et ca supprime toute possibilite de double reservation en cas de requetes
 * concurrentes sur le meme creneau.
 */
public final class Store {

    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Map<String, Map<String, Object>> SLOTS = new LinkedHashMap<>();

    private Store() {}

    public static class BookResult {
        public boolean ok;
        public String error;
        public Map<String, Object> slot;
    }

    public static void load(Path file) {
        LOCK.lock();
        try {
            SLOTS.clear();
            if (!Files.exists(file)) return;
            String text = Files.readString(file, StandardCharsets.UTF_8);
            if (text.isBlank()) return;
            List<Object> raw = Json.asArray(Json.parse(text));
            for (Object o : raw) {
                Map<String, Object> slot = Json.asObject(o);
                SLOTS.put((String) slot.get("id"), slot);
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger " + file, e);
        } finally {
            LOCK.unlock();
        }
    }

    public static void save(Path file) {
        LOCK.lock();
        try {
            Files.createDirectories(file.getParent());
            List<Object> list = new ArrayList<>(SLOTS.values());
            Files.writeString(file, Json.write(list), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'ecrire " + file, e);
        } finally {
            LOCK.unlock();
        }
    }

    /** Renvoie les creneaux, filtres par date si fournie, tries par date puis heure. */
    public static List<Object> listSlots(String date) {
        LOCK.lock();
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> slot : SLOTS.values()) {
                if (date == null || date.equals(slot.get("date"))) {
                    result.add(slot);
                }
            }
            result.sort((a, b) -> {
                int cmp = String.valueOf(a.get("date")).compareTo(String.valueOf(b.get("date")));
                if (cmp != 0) return cmp;
                return String.valueOf(a.get("time")).compareTo(String.valueOf(b.get("time")));
            });
            return new ArrayList<>(result);
        } finally {
            LOCK.unlock();
        }
    }

    public static Map<String, Object> createSlot(String date, String time, int capacity) {
        LOCK.lock();
        try {
            String id = UUID.randomUUID().toString();
            Map<String, Object> slot = new LinkedHashMap<>();
            slot.put("id", id);
            slot.put("date", date);
            slot.put("time", time);
            slot.put("capacity", (double) Math.max(1, capacity));
            slot.put("bookings", new ArrayList<>());
            SLOTS.put(id, slot);
            return slot;
        } finally {
            LOCK.unlock();
        }
    }

    public static boolean deleteSlot(String id) {
        LOCK.lock();
        try {
            Map<String, Object> slot = SLOTS.get(id);
            if (slot == null) return false;
            List<Object> bookings = Json.asArray(slot.get("bookings"));
            if (!bookings.isEmpty()) return false;
            SLOTS.remove(id);
            return true;
        } finally {
            LOCK.unlock();
        }
    }

    /** Reserve un creneau. Empeche le double-booking : refuse si la capacite est atteinte. */
    public static BookResult book(String slotId, String name, String email) {
        LOCK.lock();
        try {
            BookResult result = new BookResult();
            Map<String, Object> slot = SLOTS.get(slotId);
            if (slot == null) {
                result.ok = false;
                result.error = "creneau introuvable";
                return result;
            }
            List<Object> bookings = Json.asArray(slot.get("bookings"));
            double capacity = Json.num(slot, "capacity", 1);
            if (bookings.size() >= capacity) {
                result.ok = false;
                result.error = "ce creneau est deja complet";
                return result;
            }
            for (Object o : bookings) {
                Map<String, Object> b = Json.asObject(o);
                if (email.equalsIgnoreCase(String.valueOf(b.get("email")))) {
                    result.ok = false;
                    result.error = "vous avez deja reserve ce creneau";
                    return result;
                }
            }
            Map<String, Object> booking = new LinkedHashMap<>();
            booking.put("id", UUID.randomUUID().toString());
            booking.put("name", name);
            booking.put("email", email);
            bookings.add(booking);
            slot.put("bookings", bookings);
            result.ok = true;
            result.slot = slot;
            return result;
        } finally {
            LOCK.unlock();
        }
    }
}
