package dev.jihed.socialpub.core;

import dev.jihed.socialpub.api.Platform;
import java.time.Instant;
import java.util.UUID;

/** One audit row per publish attempt against one platform target. */
public record PublicationAttempt(
    UUID publicationId,
    Platform platform,
    int attemptNo,
    Instant startedAt,
    Instant finishedAt,
    Integer httpStatus,
    String responseExcerpt) {}
