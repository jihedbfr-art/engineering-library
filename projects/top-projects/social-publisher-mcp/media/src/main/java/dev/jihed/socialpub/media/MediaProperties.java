package dev.jihed.socialpub.media;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Object-storage settings for media staging. Bound from {@code media.minio.*}. */
@ConfigurationProperties(prefix = "media.minio")
public record MediaProperties(
    String endpoint,
    String accessKey,
    String secretKey,
    String bucket,
    Integer presignExpirySeconds) {

  public MediaProperties {
    if (bucket == null || bucket.isBlank()) {
      bucket = "media-staging";
    }
    if (presignExpirySeconds == null || presignExpirySeconds <= 0) {
      presignExpirySeconds = 3600;
    }
  }
}
