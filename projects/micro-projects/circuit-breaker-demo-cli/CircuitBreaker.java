import java.util.Random;

public class CircuitBreaker {
    enum State { CLOSED, OPEN, HALF_OPEN }

    State state = State.CLOSED;
    int failureCount = 0;
    final int failureThreshold = 3;
    long openedAt = 0;
    final long resetTimeoutMs = 2000;

    boolean call(boolean simulateFailure) {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - openedAt > resetTimeoutMs) {
                state = State.HALF_OPEN;
            } else {
                System.out.println("  -> circuit OPEN, appel rejete sans toucher le service");
                return false;
            }
        }
        if (simulateFailure) {
            failureCount++;
            System.out.println("  -> echec (" + failureCount + "/" + failureThreshold + ")");
            if (failureCount >= failureThreshold) {
                state = State.OPEN;
                openedAt = System.currentTimeMillis();
                System.out.println("  -> circuit passe a OPEN");
            }
            return false;
        }
        failureCount = 0;
        if (state == State.HALF_OPEN) System.out.println("  -> succes en HALF_OPEN, circuit referme");
        state = State.CLOSED;
        return true;
    }

    public static void main(String[] args) throws InterruptedException {
        CircuitBreaker cb = new CircuitBreaker();
        Random r = new Random(42);
        boolean[] pattern = {true, true, true, true, false, true};
        for (boolean fail : pattern) {
            System.out.println("Appel (etat=" + cb.state + ")");
            cb.call(fail);
            Thread.sleep(500);
        }
    }
}
