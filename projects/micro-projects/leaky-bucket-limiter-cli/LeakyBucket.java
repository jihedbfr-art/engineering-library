public class LeakyBucket {
    private final int capacity;
    private double water = 0;
    private final double leakRatePerSec;
    private long lastLeak = System.currentTimeMillis();

    public LeakyBucket(int capacity, double leakRatePerSec) {
        this.capacity = capacity;
        this.leakRatePerSec = leakRatePerSec;
    }

    private void leak() {
        long now = System.currentTimeMillis();
        double elapsedSec = (now - lastLeak) / 1000.0;
        water = Math.max(0, water - elapsedSec * leakRatePerSec);
        lastLeak = now;
    }

    public boolean allow() {
        leak();
        if (water + 1 > capacity) return false;
        water += 1;
        return true;
    }

    public static void main(String[] args) throws InterruptedException {
        LeakyBucket bucket = new LeakyBucket(3, 1);
        for (int i = 0; i < 5; i++) {
            System.out.println("Requete " + (i + 1) + " : " + (bucket.allow() ? "autorisee" : "lissee/rejetee"));
        }
    }
}
