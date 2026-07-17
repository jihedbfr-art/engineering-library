import java.util.HashMap;
import java.util.Map;

public class BarcodeLookup {
    static final Map<String, Product> CATALOG = new HashMap<>();

    record Product(String name, double price, int stock) {}

    static {
        CATALOG.put("3017620422003", new Product("Pate a tartiner", 4.50, 120));
        CATALOG.put("5449000000996", new Product("Soda cola 1.5L", 1.80, 300));
        CATALOG.put("7622210449283", new Product("Biscuits chocolat", 2.30, 85));
    }

    static boolean isValidEan13(String barcode) {
        if (barcode.length() != 13 || !barcode.chars().allMatch(Character::isDigit)) return false;
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = barcode.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit == (barcode.charAt(12) - '0');
    }

    public static void main(String[] args) {
        String barcode = args.length > 0 ? args[0] : "3017620422003";
        if (!isValidEan13(barcode)) {
            System.out.println("Code-barres EAN-13 invalide (check digit incorrect)");
            return;
        }
        Product product = CATALOG.get(barcode);
        System.out.println(product != null ? product : "Produit non trouve dans ce catalogue local");
    }
}
