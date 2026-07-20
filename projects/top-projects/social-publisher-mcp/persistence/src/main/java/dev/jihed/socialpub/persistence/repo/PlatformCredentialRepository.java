package dev.jihed.socialpub.persistence.repo;

import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.persistence.entity.PlatformCredentialEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformCredentialRepository
    extends JpaRepository<PlatformCredentialEntity, UUID> {

  Optional<PlatformCredentialEntity> findByPlatform(Platform platform);
}
