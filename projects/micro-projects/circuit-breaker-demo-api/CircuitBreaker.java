public class CircuitBreaker {
    enum State { CLOSED, OPEN, HALF_OPEN }

    private State state = State.CLOSED;
    private int failureCount = 0;
    private final int failureThreshold;
    private long openedAt = 0;
    private final long resetTimeoutMs;

    public CircuitBreaker(int failureThreshold, long resetTimeoutMs) {
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMs = resetTimeoutMs;
    }

    public boolean allowRequest() {
        if (state == State.OPEN && System.currentTimeMillis() - openedAt > resetTimeoutMs) {
            state = State.HALF_OPEN;
        }
        return state != State.OPEN;
    }

    public void recordSuccess() {
        failureCount = 0;
        state = State.CLOSED;
    }

    public void recordFailure() {
        failureCount++;
        if (failureCount >= failureThreshold) {
            state = State.OPEN;
            openedAt = System.currentTimeMillis();
        }
    }

    public static void main(String[] args) {
        CircuitBreaker breaker = new CircuitBreaker(3, 5000);
        for (int i = 0; i < 4; i++) {
            System.out.println("Requete autorisee : " + breaker.allowRequest());
            breaker.recordFailure();
        }
        System.out.println("Etat final : " + breaker.state);
    }
}
