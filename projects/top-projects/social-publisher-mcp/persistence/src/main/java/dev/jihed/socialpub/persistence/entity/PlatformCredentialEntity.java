package dev.jihed.socialpub.persistence.entity;

import dev.jihed.socialpub.api.Platform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platform_credential")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlatformCredentialEntity {

  @Id private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true, length = 32)
  private Platform platform;

  @Column(name = "payload_encrypted", nullable = false)
  private byte[] payloadEncrypted;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public void rotate(byte[] payloadEncrypted, Instant expiresAt) {
    this.payloadEncrypted = payloadEncrypted;
    this.expiresAt = expiresAt;
    this.updatedAt = Instant.now();
  }
}
