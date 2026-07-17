import java.math.BigInteger;

public class IbanValidator {
    static boolean isValid(String iban) {
        iban = iban.replaceAll("\s+", "").toUpperCase();
        if (iban.length() < 15) return false;
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            numeric.append(Character.isLetter(c) ? (c - 'A' + 10) : c);
        }
        return new BigInteger(numeric.toString()).mod(BigInteger.valueOf(97)).intValue() == 1;
    }

    public static void main(String[] args) {
        String iban = args.length > 0 ? args[0] : "FR1420041010050500013M02606";
        System.out.println(iban + " -> " + (isValid(iban) ? "IBAN valide" : "IBAN invalide"));
    }
}
