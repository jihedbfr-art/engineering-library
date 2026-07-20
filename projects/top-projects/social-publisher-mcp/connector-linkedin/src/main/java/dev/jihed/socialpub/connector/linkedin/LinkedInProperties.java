package dev.jihed.socialpub.connector.linkedin;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LinkedIn connector config. {@code authorType} is PERSON or ORGANIZATION; {@code authorId} is the
 * bare id that gets wrapped into {@code urn:li:person:{id}} or {@code urn:li:organization:{id}}.
 */
@ConfigurationProperties("socialpub.connectors.linkedin")
public record LinkedInProperties(
    String baseUrl, String version, String authorType, String authorId) {

  public LinkedInProperties {
    if (baseUrl == null || baseUrl.isBlank()) {
      baseUrl = "https://api.linkedin.com";
    }
    if (version == null || version.isBlank()) {
      version = "202401";
    }
    if (authorType == null || authorType.isBlank()) {
      authorType = "PERSON";
    }
  }

  public String authorUrn() {
    String prefix =
        "ORGANIZATION".equalsIgnoreCase(authorType) ? "urn:li:organization:" : "urn:li:person:";
    return prefix + authorId;
  }
}
