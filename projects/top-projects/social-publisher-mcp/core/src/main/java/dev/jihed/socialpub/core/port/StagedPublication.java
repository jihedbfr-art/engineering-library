package dev.jihed.socialpub.core.port;

import dev.jihed.socialpub.core.PublishRequest;
import java.util.UUID;

/** A persisted publication reloaded with enough context (request + staged media) to (re)publish. */
public record StagedPublication(UUID id, PublishRequest request) {}
