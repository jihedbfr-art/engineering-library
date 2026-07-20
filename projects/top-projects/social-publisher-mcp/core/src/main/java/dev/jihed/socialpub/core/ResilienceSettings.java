package dev.jihed.socialpub.core;

import java.time.Duration;

/**
 * Tuning for the per-platform retry/rate-limit around each publish. Kept as a plain value so {@code
 * core} stays free of Spring config binding; the app maps its properties onto this.
 *
 * @param maxAttempts total attempts including the first
 * @param initialBackoff wait before the second attempt; doubles each retry
 * @param permitsPerSecond per-platform rate limit
 */
public record ResilienceSettings(
    int maxAttempts, Duration initialBackoff, double permitsPerSecond) {

  public static ResilienceSettings defaults() {
    return new ResilienceSettings(3, Duration.ofMillis(500), 5.0);
  }
}
