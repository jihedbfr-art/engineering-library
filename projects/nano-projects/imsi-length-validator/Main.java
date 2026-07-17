public class Main {
    static boolean isValidImsi(String imsi) {
        return imsi.chars().allMatch(Character::isDigit) && imsi.length() >= 14 && imsi.length() <= 15;
    }

    public static void main(String[] args) {
        String imsi = args.length > 0 ? args[0] : "605020123456789";
        System.out.println(imsi + " -> " + (isValidImsi(imsi) ? "longueur IMSI valide" : "longueur invalide"));
    }
}
