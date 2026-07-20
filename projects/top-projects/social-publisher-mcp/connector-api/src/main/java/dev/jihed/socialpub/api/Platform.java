package dev.jihed.socialpub.api;

/**
 * The social networks this server can publish to.
 *
 * <p>{@link #MOCK} is not a real network. It exists so the project can be demoed end to end without
 * live credentials and is only wired in under the {@code dev} profile.
 */
public enum Platform {
  INSTAGRAM,
  FACEBOOK,
  TIKTOK,
  LINKEDIN,
  X,
  YOUTUBE,
  MOCK
}
