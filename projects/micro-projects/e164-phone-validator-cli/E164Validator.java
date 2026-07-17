import java.util.regex.Pattern;

public class E164Validator {
    static final Pattern E164 = Pattern.compile("^\+[1-9]\d{6,14}$");

    public static void main(String[] args) {
        String number = args.length > 0 ? args[0] : "+21625123456";
        boolean valid = E164.matcher(number).matches();
        System.out.println(number + " -> " + (valid ? "format E.164 valide" : "format invalide (attendu: +<pays><numero>)"));
    }
}
