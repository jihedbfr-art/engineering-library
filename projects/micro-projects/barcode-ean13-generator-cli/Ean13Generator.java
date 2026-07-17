public class Ean13Generator {
    static int checkDigit(String digits12) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int d = digits12.charAt(i) - '0';
            sum += (i % 2 == 0) ? d : d * 3;
        }
        return (10 - (sum % 10)) % 10;
    }

    public static void main(String[] args) {
        String digits12 = args.length > 0 ? args[0] : "500123456789";
        int check = checkDigit(digits12);
        String ean13 = digits12 + check;
        System.out.println("EAN-13 : " + ean13);
        System.out.println(ean13.replaceAll(".", "$0 ").trim());
    }
}
