package dev.jihed.socialpub.core;

import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOverrides;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A cross-post request. {@code scheduledAt} null means publish now. {@code overrides} lets a caller
 * tailor the caption/hashtags/title per platform; a missing entry falls back to the shared caption.
 */
public record PublishRequest(
    String caption,
    List<MediaRef> media,
    Set<Platform> targets,
    Instant scheduledAt,
    Map<Platform, PlatformOverrides> overrides) {

  public PublishRequest {
    media = media == null ? List.of() : List.copyOf(media);
    targets = targets == null ? Set.of() : Set.copyOf(targets);
    overrides = overrides == null ? Map.of() : Map.copyOf(overrides);
  }

  public PlatformOverrides overridesFor(Platform platform) {
    return overrides.getOrDefault(platform, PlatformOverrides.none());
  }

  public boolean isScheduled(Instant now) {
    return scheduledAt != null && scheduledAt.isAfter(now);
  }
}
