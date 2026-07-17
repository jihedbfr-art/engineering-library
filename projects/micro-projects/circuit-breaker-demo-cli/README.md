# circuit-breaker-demo-cli

Simule une machine a etats de circuit breaker (CLOSED -> OPEN -> HALF_OPEN -> CLOSED) sur
une sequence d'appels reussis/echoues - le pattern de resilience derriere Resilience4j/Hystrix.

## Lancer

```bash
javac CircuitBreaker.java && java CircuitBreaker
```
