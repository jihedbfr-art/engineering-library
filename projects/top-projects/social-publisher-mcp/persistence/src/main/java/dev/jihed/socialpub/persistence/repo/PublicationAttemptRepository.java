package dev.jihed.socialpub.persistence.repo;

import dev.jihed.socialpub.persistence.entity.PublicationAttemptEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationAttemptRepository
    extends JpaRepository<PublicationAttemptEntity, UUID> {}
