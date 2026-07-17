public class Main {
    static int toGray(int n) {
        return n ^ (n >> 1);
    }

    public static void main(String[] args) {
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 10;
        System.out.println(n + " -> Gray: " + Integer.toBinaryString(toGray(n)));
    }
}
