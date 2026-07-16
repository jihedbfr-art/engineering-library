import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Logique coeur d'un raccourcisseur d'URL : stockage en memoire (HashMap), code court
 * genere en base62 sur 6 caracteres. Aucun framework, aucun serveur HTTP.
 */
public class UrlShortener {

    private static final String BASE62_ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_GENERATION_ATTEMPTS = 100;

    private final Map<String, String> codeToUrl = new HashMap<>();
    private final Map<String, String> urlToCode = new HashMap<>();
    private final Random random;

    public UrlShortener() {
        this.random = new Random();
    }

    /** Constructeur avec seed, pour des tests/demos reproductibles. */
    public UrlShortener(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Raccourcit une URL et retourne son code. Si l'URL a deja ete raccourcie,
     * retourne le code existant (pas de doublon).
     */
    public String shorten(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("L'URL ne peut pas etre vide");
        }

        String existingCode = urlToCode.get(url);
        if (existingCode != null) {
            return existingCode;
        }

        String code = generateUniqueCode();
        codeToUrl.put(code, url);
        urlToCode.put(url, code);
        return code;
    }

    /** Resout un code vers son URL d'origine, ou null si le code est inconnu. */
    public String resolve(String code) {
        return codeToUrl.get(code);
    }

    public int size() {
        return codeToUrl.size();
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String code = randomCode();
            if (!codeToUrl.containsKey(code)) {
                return code;
            }
        }
        throw new IllegalStateException(
                "Impossible de generer un code unique apres " + MAX_GENERATION_ATTEMPTS + " tentatives");
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int idx = random.nextInt(BASE62_ALPHABET.length());
            sb.append(BASE62_ALPHABET.charAt(idx));
        }
        return sb.toString();
    }
}
