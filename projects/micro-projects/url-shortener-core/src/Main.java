/**
 * Demonstration console de UrlShortener : shorten() / resolve(), gestion des doublons,
 * et resolution d'un code inconnu.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Demo UrlShortener ===\n");

        UrlShortener shortener = new UrlShortener(42);

        String url1 = "https://jihedapps.dev/knowledge/engineering-cookbook";
        String url2 = "https://jihedapps.dev/projects/notes-app-microservices";

        String code1 = shortener.shorten(url1);
        System.out.println("shorten(\"" + url1 + "\") -> " + code1);

        String code2 = shortener.shorten(url2);
        System.out.println("shorten(\"" + url2 + "\") -> " + code2);

        System.out.println("\nresolve(\"" + code1 + "\") -> " + shortener.resolve(code1));
        System.out.println("resolve(\"" + code2 + "\") -> " + shortener.resolve(code2));

        System.out.println("\nRe-shorten de la meme URL (doit renvoyer le meme code, pas de doublon):");
        String code1Again = shortener.shorten(url1);
        System.out.println("shorten(\"" + url1 + "\") -> " + code1Again
                + " (identique a " + code1 + " ? " + code1.equals(code1Again) + ")");

        System.out.println("\nresolve(\"CODEINCONNU\") -> " + shortener.resolve("CODEINCONNU") + " (attendu: null)");

        System.out.println("\nNombre total d'URLs stockees: " + shortener.size() + " (attendu: 2, pas de doublon)");
    }
}
