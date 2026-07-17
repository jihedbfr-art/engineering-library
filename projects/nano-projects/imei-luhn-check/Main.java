public class Main {
    static boolean isValidImei(String imei) {
        if (imei.length() != 15 || !imei.chars().allMatch(Character::isDigit)) return false;
        int sum = 0;
        for (int i = 0; i < 15; i++) {
            int digit = imei.charAt(i) - '0';
            if (i % 2 == 1) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
        }
        return sum % 10 == 0;
    }

    public static void main(String[] args) {
        String imei = args.length > 0 ? args[0] : "490154203237518";
        System.out.println(imei + " -> " + (isValidImei(imei) ? "IMEI valide (Luhn OK)" : "IMEI invalide"));
    }
}
