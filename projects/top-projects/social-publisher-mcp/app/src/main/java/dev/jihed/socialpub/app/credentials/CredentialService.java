package dev.jihed.socialpub.app.credentials;

import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.persistence.entity.PlatformCredentialEntity;
import dev.jihed.socialpub.persistence.repo.PlatformCredentialRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores and reads per-platform tokens. Payloads are opaque JSON strings that each connector
 * interprets; they are encrypted with {@link CredentialCipher} before hitting the database.
 */
@Service
public class CredentialService {

  public record ConnectedAccount(Platform platform, Instant expiresAt, boolean valid) {}

  private final PlatformCredentialRepository repository;
  private final CredentialCipher cipher;

  public CredentialService(PlatformCredentialRepository repository, CredentialCipher cipher) {
    this.repository = repository;
    this.cipher = cipher;
  }

  @Transactional
  public void store(Platform platform, String payloadJson, Instant expiresAt) {
    byte[] encrypted = cipher.encrypt(payloadJson);
    repository
        .findByPlatform(platform)
        .ifPresentOrElse(
            existing -> existing.rotate(encrypted, expiresAt),
            () ->
                repository.save(
                    PlatformCredentialEntity.builder()
                        .id(UUID.randomUUID())
                        .platform(platform)
                        .payloadEncrypted(encrypted)
                        .expiresAt(expiresAt)
                        .updatedAt(Instant.now())
                        .build()));
  }

  @Transactional(readOnly = true)
  public Optional<String> load(Platform platform) {
    return repository.findByPlatform(platform).map(e -> cipher.decrypt(e.getPayloadEncrypted()));
  }

  @Transactional(readOnly = true)
  public List<ConnectedAccount> listConnected() {
    Instant now = Instant.now();
    return repository.findAll().stream()
        .map(
            e ->
                new ConnectedAccount(
                    e.getPlatform(),
                    e.getExpiresAt(),
                    e.getExpiresAt() == null || e.getExpiresAt().isAfter(now)))
        .toList();
  }
}
