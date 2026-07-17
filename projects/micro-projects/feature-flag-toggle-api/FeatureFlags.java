import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class FeatureFlags {
    private final Map<String, Boolean> flags = new ConcurrentHashMap<>();

    public void set(String flag, boolean enabled) {
        flags.put(flag, enabled);
    }

    public boolean isEnabled(String flag) {
        return flags.getOrDefault(flag, false);
    }

    public static void main(String[] args) {
        FeatureFlags flags = new FeatureFlags();
        flags.set("new-checkout-flow", true);
        flags.set("dark-mode", false);

        System.out.println("new-checkout-flow: " + flags.isEnabled("new-checkout-flow"));
        System.out.println("dark-mode: " + flags.isEnabled("dark-mode"));
        System.out.println("unknown-flag (defaut): " + flags.isEnabled("unknown-flag"));
    }
}
