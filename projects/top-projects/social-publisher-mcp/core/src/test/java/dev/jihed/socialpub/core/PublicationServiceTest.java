package dev.jihed.socialpub.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.MediaType;
import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.api.PublishCommand;
import dev.jihed.socialpub.api.SocialPublisher;
import dev.jihed.socialpub.core.port.MediaStager;
import dev.jihed.socialpub.core.port.MediaValidator;
import dev.jihed.socialpub.core.port.StagedPublication;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PublicationServiceTest {

  private final ExecutorService executor = Executors.newFixedThreadPool(4);
  private final InMemoryPublicationStore store = new InMemoryPublicationStore();
  private final MediaStager passthroughStager = media -> media;
  private final ResilienceSettings fastRetry =
      new ResilienceSettings(3, Duration.ofMillis(1), 1000.0);

  @AfterEach
  void tearDown() {
    executor.shutdownNow();
  }

  private PublicationService service(
      Map<Platform, SocialPublisher> publishers, MediaValidator validator) {
    return new PublicationService(
        publishers, passthroughStager, validator, store, fastRetry, executor);
  }

  private static SocialPublisher fixed(Platform platform, PlatformOutcome outcome) {
    return new SocialPublisher() {
      @Override
      public Platform platform() {
        return platform;
      }

      @Override
      public PlatformOutcome publish(PublishCommand command) {
        return outcome;
      }
    };
  }

  private static PublishRequest request(Set<Platform> targets) {
    return new PublishRequest(
        "hello world",
        List.of(MediaRef.ofUrl("https://example.com/a.jpg", MediaType.IMAGE)),
        targets,
        null,
        Map.of());
  }

  private static final MediaValidator NO_VIOLATIONS = (media, targets) -> List.of();

  @Test
  void publishNow_allSuccess_isPublished() {
    var publishers =
        Map.of(
            Platform.LINKEDIN, fixed(Platform.LINKEDIN, PlatformOutcome.ok("li-1", "url-li")),
            Platform.X, fixed(Platform.X, PlatformOutcome.ok("x-1", "url-x")));

    PublishResult result =
        service(publishers, NO_VIOLATIONS).publish(request(Set.of(Platform.LINKEDIN, Platform.X)));

    assertThat(result.overallStatus()).isEqualTo(PublicationStatus.PUBLISHED);
    assertThat(result.outcomes()).hasSize(2).allSatisfy((p, o) -> assertThat(o.success()).isTrue());
    assertThat(store.statuses.get(result.publicationId())).isEqualTo(PublicationStatus.PUBLISHED);
  }

  @Test
  void publishNow_mixed_isPartiallyPublished() {
    var publishers =
        Map.of(
            Platform.LINKEDIN, fixed(Platform.LINKEDIN, PlatformOutcome.ok("li-1", "url-li")),
            Platform.X, fixed(Platform.X, PlatformOutcome.fail("AUTH", "bad token", false)));

    PublishResult result =
        service(publishers, NO_VIOLATIONS).publish(request(Set.of(Platform.LINKEDIN, Platform.X)));

    assertThat(result.overallStatus()).isEqualTo(PublicationStatus.PARTIALLY_PUBLISHED);
    assertThat(result.outcomes().get(Platform.X).success()).isFalse();
  }

  @Test
  void publish_emptyTargets_isRejected() {
    var service = service(Map.of(), NO_VIOLATIONS);
    assertThatThrownBy(() -> service.publish(request(Set.of())))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("no target platforms");
  }

  @Test
  void publish_unknownConnector_isRejected() {
    var service = service(Map.of(), NO_VIOLATIONS);
    assertThatThrownBy(() -> service.publish(request(Set.of(Platform.TIKTOK))))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("no connector registered for TIKTOK");
  }

  @Test
  void publish_mediaViolations_areRejected() {
    var publishers = Map.of(Platform.X, fixed(Platform.X, PlatformOutcome.ok("x", "u")));
    MediaValidator failing = (media, targets) -> List.of("file too big for X");
    var service = service(publishers, failing);
    assertThatThrownBy(() -> service.publish(request(Set.of(Platform.X))))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("too big");
  }

  @Test
  void scheduledInFuture_isPersistedButNotFannedOut() {
    AtomicInteger calls = new AtomicInteger();
    SocialPublisher counting =
        new SocialPublisher() {
          @Override
          public Platform platform() {
            return Platform.LINKEDIN;
          }

          @Override
          public PlatformOutcome publish(PublishCommand command) {
            calls.incrementAndGet();
            return PlatformOutcome.ok("li", "u");
          }
        };
    var req =
        new PublishRequest(
            "later",
            List.of(),
            Set.of(Platform.LINKEDIN),
            Instant.now().plus(Duration.ofHours(1)),
            Map.of());

    PublishResult result = service(Map.of(Platform.LINKEDIN, counting), NO_VIOLATIONS).publish(req);

    assertThat(calls).hasValue(0);
    assertThat(result.outcomes()).isEmpty();
    assertThat(store.statuses.get(result.publicationId())).isEqualTo(PublicationStatus.SCHEDULED);
  }

  @Test
  void transientFailure_isRetriedThenSucceeds() {
    AtomicInteger attempts = new AtomicInteger();
    SocialPublisher flaky =
        new SocialPublisher() {
          @Override
          public Platform platform() {
            return Platform.X;
          }

          @Override
          public PlatformOutcome publish(PublishCommand command) {
            if (attempts.incrementAndGet() < 3) {
              return PlatformOutcome.fail("RATE_LIMIT", "slow down", true);
            }
            return PlatformOutcome.ok("x-ok", "url");
          }
        };

    PublishResult result =
        service(Map.of(Platform.X, flaky), NO_VIOLATIONS).publish(request(Set.of(Platform.X)));

    assertThat(attempts).hasValue(3);
    assertThat(result.outcomes().get(Platform.X).success()).isTrue();
  }

  @Test
  void nonRetryableFailure_isNotRetried() {
    AtomicInteger attempts = new AtomicInteger();
    SocialPublisher hardFail =
        new SocialPublisher() {
          @Override
          public Platform platform() {
            return Platform.X;
          }

          @Override
          public PlatformOutcome publish(PublishCommand command) {
            attempts.incrementAndGet();
            return PlatformOutcome.fail("AUTH", "revoked", false);
          }
        };

    service(Map.of(Platform.X, hardFail), NO_VIOLATIONS).publish(request(Set.of(Platform.X)));

    assertThat(attempts).hasValue(1);
  }

  @Test
  void thrownException_becomesRetryableThenFails() {
    AtomicInteger attempts = new AtomicInteger();
    SocialPublisher throwing =
        new SocialPublisher() {
          @Override
          public Platform platform() {
            return Platform.X;
          }

          @Override
          public PlatformOutcome publish(PublishCommand command) {
            attempts.incrementAndGet();
            throw new IllegalStateException("boom");
          }
        };

    PublishResult result =
        service(Map.of(Platform.X, throwing), NO_VIOLATIONS).publish(request(Set.of(Platform.X)));

    assertThat(attempts).hasValue(3); // exhausted all retries on exceptions
    assertThat(result.outcomes().get(Platform.X).success()).isFalse();
  }

  @Test
  void scheduled_thenClaimedAndExecuted() {
    SocialPublisher ok = fixed(Platform.LINKEDIN, PlatformOutcome.ok("li", "u"));
    var service = service(Map.of(Platform.LINKEDIN, ok), NO_VIOLATIONS);
    var req =
        new PublishRequest(
            "due", List.of(), Set.of(Platform.LINKEDIN), Instant.now().minusSeconds(1), Map.of());
    // scheduledAt is in the past, so publish() fans out immediately
    PublishResult immediate = service.publish(req);
    assertThat(immediate.overallStatus()).isEqualTo(PublicationStatus.PUBLISHED);

    // and a genuinely scheduled one can be claimed and executed
    UUID scheduledId =
        store.create(
            new PublishRequest(
                "future",
                List.of(),
                Set.of(Platform.LINKEDIN),
                Instant.now().minusSeconds(1),
                Map.of()),
            PublicationStatus.SCHEDULED);
    List<StagedPublication> due = service.claimDue(Instant.now(), 10);
    assertThat(due).extracting(StagedPublication::id).contains(scheduledId);
    PublishResult executed = service.executeStaged(due.get(0));
    assertThat(executed.overallStatus()).isEqualTo(PublicationStatus.PUBLISHED);
  }

  @Test
  void cancelScheduled_onlyWorksWhenScheduled() {
    UUID id =
        store.create(
            new PublishRequest(
                "x", List.of(), Set.of(Platform.X), Instant.now().plusSeconds(3600), Map.of()),
            PublicationStatus.SCHEDULED);
    var service =
        service(Map.of(Platform.X, fixed(Platform.X, PlatformOutcome.ok("x", "u"))), NO_VIOLATIONS);
    assertThat(service.cancelScheduled(id)).isTrue();
    assertThat(service.cancelScheduled(id)).isFalse(); // already cancelled
  }
}
