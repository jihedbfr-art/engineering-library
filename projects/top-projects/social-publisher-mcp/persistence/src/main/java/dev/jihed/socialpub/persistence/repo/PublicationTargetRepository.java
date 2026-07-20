package dev.jihed.socialpub.persistence.repo;

import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.persistence.entity.PublicationTargetEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationTargetRepository extends JpaRepository<PublicationTargetEntity, UUID> {

  Optional<PublicationTargetEntity> findByPublicationIdAndPlatform(
      UUID publicationId, Platform platform);
}
