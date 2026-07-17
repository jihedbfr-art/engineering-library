import java.util.HashSet;
import java.util.Set;

public class Main {
    static boolean isHappy(int n) {
        Set<Integer> seen = new HashSet<>();
        while (n != 1 && !seen.contains(n)) {
            seen.add(n);
            int sum = 0;
            while (n > 0) {
                int d = n % 10;
                sum += d * d;
                n /= 10;
            }
            n = sum;
        }
        return n == 1;
    }

    public static void main(String[] args) {
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 19;
        System.out.println(n + " -> " + (isHappy(n) ? "nombre heureux" : "pas heureux"));
    }
}
