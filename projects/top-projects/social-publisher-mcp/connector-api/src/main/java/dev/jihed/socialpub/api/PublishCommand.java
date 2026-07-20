package dev.jihed.socialpub.api;

import java.util.List;

/**
 * The unit of work handed to a single {@link SocialPublisher}. The orchestrator has already
 * resolved the effective caption (request caption merged with any {@link PlatformOverrides}) and
 * staged the media, so a connector only has to talk to its platform's API.
 */
public record PublishCommand(String caption, List<MediaRef> media, PlatformOverrides overrides) {

  /** The caption a connector should actually post: the override wins when present. */
  public String effectiveCaption() {
    if (overrides != null && overrides.caption() != null && !overrides.caption().isBlank()) {
      return overrides.caption();
    }
    return caption;
  }
}
