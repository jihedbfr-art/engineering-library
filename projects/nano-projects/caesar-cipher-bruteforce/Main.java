public class Main {
    static String shift(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                sb.append((char) ((c - base + n) % 26 + base));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String cipher = args.length > 0 ? args[0] : "Khoor Zruog";
        for (int shiftAmount = 0; shiftAmount < 26; shiftAmount++) {
            System.out.println(shiftAmount + ": " + shift(cipher, -shiftAmount));
        }
    }
}
