package dev.jihed.socialpub.connector.youtube;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** YouTube Data API v3 connector config. Shorts mode appends {@code #Shorts} to the title. */
@ConfigurationProperties("socialpub.connectors.youtube")
public record YouTubeProperties(String baseUrl, Boolean shortsMode, String privacyStatus) {

  public YouTubeProperties {
    if (baseUrl == null || baseUrl.isBlank()) {
      baseUrl = "https://www.googleapis.com";
    }
    if (shortsMode == null) {
      shortsMode = true;
    }
    if (privacyStatus == null || privacyStatus.isBlank()) {
      privacyStatus = "unlisted";
    }
  }
}
