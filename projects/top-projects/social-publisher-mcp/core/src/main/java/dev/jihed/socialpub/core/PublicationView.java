package dev.jihed.socialpub.core;

import dev.jihed.socialpub.api.Platform;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Read model returned to MCP clients for status / history tools. */
public record PublicationView(
    UUID id,
    String caption,
    PublicationStatus status,
    Instant scheduledAt,
    Instant createdAt,
    List<Target> targets) {

  public record Target(
      Platform platform,
      PublicationStatus status,
      String externalPostId,
      String permalink,
      String errorCode,
      String errorMessage) {}
}
