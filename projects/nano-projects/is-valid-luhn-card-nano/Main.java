public class Main {
    static boolean luhnValid(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = number.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    public static void main(String[] args) {
        String number = args.length > 0 ? args[0] : "4539578763621486";
        System.out.println(number + " -> " + (luhnValid(number) ? "valide" : "invalide"));
    }
}
