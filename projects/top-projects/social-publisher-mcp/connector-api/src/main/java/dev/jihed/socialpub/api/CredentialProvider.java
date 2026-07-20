package dev.jihed.socialpub.api;

import java.util.Optional;

/**
 * How a connector gets the access token for its platform. The stored payload is an opaque string —
 * usually the access token itself, or a small JSON blob the connector knows how to read.
 * Implemented by the app (backed by the encrypted credential store); connectors only see this port.
 */
public interface CredentialProvider {

  Optional<String> find(Platform platform);

  default String require(Platform platform) {
    return find(platform)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "No stored credentials for "
                        + platform
                        + " — register them via POST /admin/credentials/"
                        + platform));
  }
}
