package dev.jihed.socialpub.connector.tiktok;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TikTok Content Posting API config. {@code mode} is DIRECT (publish) or DRAFT (send to the user's
 * inbox). DRAFT is the default because unaudited apps can only post privately / as drafts.
 */
@ConfigurationProperties("socialpub.connectors.tiktok")
public record TikTokProperties(String baseUrl, String mode, Integer pollAttempts) {

  public TikTokProperties {
    if (baseUrl == null || baseUrl.isBlank()) {
      baseUrl = "https://open.tiktokapis.com";
    }
    if (mode == null || mode.isBlank()) {
      mode = "DRAFT";
    }
    if (pollAttempts == null || pollAttempts < 1) {
      pollAttempts = 15;
    }
  }

  public boolean direct() {
    return "DIRECT".equalsIgnoreCase(mode);
  }
}
