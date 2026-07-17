import java.util.HashSet;
import java.util.Set;

public class Main {
    static boolean isPangram(String s) {
        Set<Character> letters = new HashSet<>();
        for (char c : s.toLowerCase().toCharArray()) {
            if (Character.isLetter(c)) letters.add(c);
        }
        return letters.size() == 26;
    }

    public static void main(String[] args) {
        String s = args.length > 0 ? String.join(" ", args) : "The quick brown fox jumps over the lazy dog";
        System.out.println(isPangram(s) ? "Pangramme" : "Pas un pangramme");
    }
}
