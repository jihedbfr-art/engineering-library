package dev.jihed.socialpub.app.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Retry/rate-limit tuning, bound from {@code socialpub.resilience.*}. */
@ConfigurationProperties(prefix = "socialpub.resilience")
public record OrchestrationProperties(
    Integer maxAttempts, Duration initialBackoff, Double permitsPerSecond) {

  public OrchestrationProperties {
    if (maxAttempts == null || maxAttempts < 1) {
      maxAttempts = 3;
    }
    if (initialBackoff == null || initialBackoff.isZero() || initialBackoff.isNegative()) {
      initialBackoff = Duration.ofMillis(500);
    }
    if (permitsPerSecond == null || permitsPerSecond <= 0) {
      permitsPerSecond = 5.0;
    }
  }
}
