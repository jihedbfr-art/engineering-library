package dev.jihed.socialpub.app.admin;

import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.app.credentials.CredentialService;
import java.time.Instant;
import java.util.Locale;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stores platform tokens. Guarded by the same API key as the MCP endpoint (see {@code
 * McpApiKeyFilter}). The payload is an opaque JSON string the matching connector knows how to read.
 */
@RestController
@RequestMapping("/admin/credentials")
public class AdminCredentialController {

  public record CredentialRequest(String payload, String expiresAt) {}

  private final CredentialService credentials;

  public AdminCredentialController(CredentialService credentials) {
    this.credentials = credentials;
  }

  @PostMapping("/{platform}")
  public ResponseEntity<String> store(
      @PathVariable String platform, @RequestBody CredentialRequest request) {
    Platform p = Platform.valueOf(platform.trim().toUpperCase(Locale.ROOT));
    Instant expiresAt =
        (request.expiresAt() == null || request.expiresAt().isBlank())
            ? null
            : Instant.parse(request.expiresAt());
    credentials.store(p, request.payload(), expiresAt);
    return ResponseEntity.ok("stored credentials for " + p);
  }
}
