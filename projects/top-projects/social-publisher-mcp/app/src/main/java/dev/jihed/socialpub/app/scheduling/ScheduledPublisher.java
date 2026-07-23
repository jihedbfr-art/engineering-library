package dev.jihed.socialpub.app.scheduling;

import dev.jihed.socialpub.core.PublicationService;
import dev.jihed.socialpub.core.port.StagedPublication;
import java.time.Instant;
import java.util.List;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polls for scheduled publications that have come due and hands them to the orchestrator. The
 * {@code @SchedulerLock} keeps a single instance doing the work even if the app is scaled out; the
 * store's {@code claimDue} uses {@code FOR UPDATE SKIP LOCKED} as a second line of defence.
 */
@Component
@Profile("!demo")
public class ScheduledPublisher {

  private static final Logger log = LoggerFactory.getLogger(ScheduledPublisher.class);

  private final PublicationService publications;
  private final int batchSize;

  public ScheduledPublisher(
      PublicationService publications,
      @Value("${socialpub.scheduling.batch-size:20}") int batchSize) {
    this.publications = publications;
    this.batchSize = batchSize;
  }

  @Scheduled(fixedDelayString = "${socialpub.scheduling.poll-interval-ms:30000}")
  @SchedulerLock(name = "publishDuePosts", lockAtLeastFor = "PT10S", lockAtMostFor = "PT5M")
  public void publishDue() {
    List<StagedPublication> due = publications.claimDue(Instant.now(), batchSize);
    if (due.isEmpty()) {
      return;
    }
    log.info("Publishing {} due scheduled post(s)", due.size());
    for (StagedPublication staged : due) {
      try {
        publications.executeStaged(staged);
      } catch (RuntimeException e) {
        log.error("Failed to publish scheduled {}", staged.id(), e);
      }
    }
  }
}
