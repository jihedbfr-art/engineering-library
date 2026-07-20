package dev.jihed.socialpub.api;

import java.util.List;

/**
 * Per-platform tweaks that override the shared caption. Any field may be null, meaning "fall back
 * to the request-level value". {@code title} is only meaningful for platforms that carry one
 * (YouTube).
 */
public record PlatformOverrides(String caption, List<String> hashtags, String title) {

  public static PlatformOverrides none() {
    return new PlatformOverrides(null, null, null);
  }
}
