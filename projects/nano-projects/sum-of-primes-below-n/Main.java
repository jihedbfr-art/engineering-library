public class Main {
    static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; (long) i * i <= n; i++) if (n % i == 0) return false;
        return true;
    }

    public static void main(String[] args) {
        int limit = args.length > 0 ? Integer.parseInt(args[0]) : 100;
        long sum = 0;
        for (int i = 2; i < limit; i++) if (isPrime(i)) sum += i;
        System.out.println("Somme des nombres premiers < " + limit + " = " + sum);
    }
}
