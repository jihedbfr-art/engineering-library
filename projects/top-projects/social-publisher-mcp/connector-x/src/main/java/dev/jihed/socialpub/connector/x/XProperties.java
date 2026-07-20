package dev.jihed.socialpub.connector.x;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * X (Twitter) connector config. v1 posts text tweets with an OAuth2 user token (read from the
 * credential store). The OAuth1 keys are here for the media-upload path, which is a roadmap item.
 */
@ConfigurationProperties("socialpub.connectors.x")
public record XProperties(
    String baseUrl, String apiKey, String apiSecret, String accessToken, String accessSecret) {

  public XProperties {
    if (baseUrl == null || baseUrl.isBlank()) {
      baseUrl = "https://api.x.com";
    }
  }
}
