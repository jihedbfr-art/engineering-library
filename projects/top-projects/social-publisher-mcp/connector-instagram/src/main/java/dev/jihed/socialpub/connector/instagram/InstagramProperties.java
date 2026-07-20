package dev.jihed.socialpub.connector.instagram;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Instagram Graph API connector config. Requires an IG professional user id. */
@ConfigurationProperties("socialpub.connectors.instagram")
public record InstagramProperties(String baseUrl, String igUserId, Integer pollAttempts) {

  public InstagramProperties {
    if (baseUrl == null || baseUrl.isBlank()) {
      baseUrl = "https://graph.facebook.com/v19.0";
    }
    if (pollAttempts == null || pollAttempts < 1) {
      pollAttempts = 15;
    }
  }
}
