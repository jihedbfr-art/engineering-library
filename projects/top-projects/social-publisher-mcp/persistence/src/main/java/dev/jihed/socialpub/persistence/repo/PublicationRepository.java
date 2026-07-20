package dev.jihed.socialpub.persistence.repo;

import dev.jihed.socialpub.core.PublicationStatus;
import dev.jihed.socialpub.persistence.entity.PublicationEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublicationRepository extends JpaRepository<PublicationEntity, java.util.UUID> {

  List<PublicationEntity> findByStatusOrderByCreatedAtDesc(PublicationStatus status);

  /**
   * Claim due scheduled publications. {@code FOR UPDATE SKIP LOCKED} lets several poller instances
   * run without handing the same row to two of them; the lock is held for the surrounding
   * transaction while the caller flips the status.
   */
  @Query(
      value =
          "select * from publication where status = 'SCHEDULED' and scheduled_at <= :now "
              + "order by scheduled_at limit :max for update skip locked",
      nativeQuery = true)
  List<PublicationEntity> lockDue(@Param("now") Instant now, @Param("max") int max);
}
