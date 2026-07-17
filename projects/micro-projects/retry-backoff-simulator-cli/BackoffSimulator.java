public class BackoffSimulator {
    public static void main(String[] args) {
        int attempts = args.length > 0 ? Integer.parseInt(args[0]) : 6;
        long baseMs = args.length > 1 ? Long.parseLong(args[1]) : 200;
        double jitterFactor = 0.2;

        for (int i = 1; i <= attempts; i++) {
            long delay = (long) (baseMs * Math.pow(2, i - 1));
            long jitter = (long) (delay * jitterFactor);
            System.out.printf("Tentative %d -> delai ~%dms (base) +/- %dms jitter%n", i, delay, jitter);
        }
    }
}
