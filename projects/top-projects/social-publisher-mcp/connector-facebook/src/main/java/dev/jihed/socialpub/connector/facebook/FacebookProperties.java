package dev.jihed.socialpub.connector.facebook;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Facebook Pages connector config. Posts go to {@code /{pageId}/...} with a Page access token. */
@ConfigurationProperties("socialpub.connectors.facebook")
public record FacebookProperties(String baseUrl, String pageId) {

  public FacebookProperties {
    if (baseUrl == null || baseUrl.isBlank()) {
      baseUrl = "https://graph.facebook.com/v19.0";
    }
  }
}
