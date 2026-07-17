import java.util.regex.Pattern;

public class VatValidator {
    static final Pattern FR = Pattern.compile("^FR[0-9A-Z]{2}\d{9}$");
    static final Pattern DE = Pattern.compile("^DE\d{9}$");
    static final Pattern IT = Pattern.compile("^IT\d{11}$");

    static boolean isValidFormat(String vat) {
        vat = vat.replaceAll("\s+", "").toUpperCase();
        return FR.matcher(vat).matches() || DE.matcher(vat).matches() || IT.matcher(vat).matches();
    }

    public static void main(String[] args) {
        String vat = args.length > 0 ? args[0] : "FR40303265045";
        System.out.println(vat + " -> " + (isValidFormat(vat) ? "format valide" : "format non reconnu (FR/DE/IT seulement)"));
    }
}
