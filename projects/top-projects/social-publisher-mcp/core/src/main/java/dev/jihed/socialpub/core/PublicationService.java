package dev.jihed.socialpub.core;

import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.api.PublishCommand;
import dev.jihed.socialpub.api.SocialPublisher;
import dev.jihed.socialpub.core.port.MediaStager;
import dev.jihed.socialpub.core.port.MediaValidator;
import dev.jihed.socialpub.core.port.PublicationStore;
import dev.jihed.socialpub.core.port.StagedPublication;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orchestrates a publish: validate, stage media, persist, fan out to the platform adapters in
 * parallel (each wrapped in retry + a per-platform rate limiter) and aggregate the per-platform
 * outcomes. It never throws on a partial failure — the outcome map is always returned.
 */
public class PublicationService {

  private static final Logger log = LoggerFactory.getLogger(PublicationService.class);
  private static final int EXCERPT_LIMIT = 500;

  private final Map<Platform, SocialPublisher> publishers;
  private final MediaStager stager;
  private final MediaValidator validator;
  private final PublicationStore store;
  private final ResilienceSettings settings;
  private final ExecutorService executor;
  private final Map<Platform, RateLimiter> rateLimiters;

  public PublicationService(
      Map<Platform, SocialPublisher> publishers,
      MediaStager stager,
      MediaValidator validator,
      PublicationStore store,
      ResilienceSettings settings,
      ExecutorService executor) {
    this.publishers = new EnumMap<>(Platform.class);
    this.publishers.putAll(publishers);
    this.stager = stager;
    this.validator = validator;
    this.store = store;
    this.settings = settings;
    this.executor = executor;
    this.rateLimiters = buildRateLimiters(publishers.keySet(), settings);
  }

  private static Map<Platform, RateLimiter> buildRateLimiters(
      java.util.Set<Platform> platforms, ResilienceSettings settings) {
    RateLimiterConfig config =
        RateLimiterConfig.custom()
            .limitForPeriod(Math.max(1, (int) Math.round(settings.permitsPerSecond())))
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofSeconds(10))
            .build();
    Map<Platform, RateLimiter> map = new EnumMap<>(Platform.class);
    for (Platform p : platforms) {
      map.put(p, RateLimiter.of("rl-" + p, config));
    }
    return map;
  }

  /** Entry point for the {@code publish_post} tool. */
  public PublishResult publish(PublishRequest request) {
    validate(request);

    List<MediaRef> staged = stager.stage(request.media());
    PublishRequest stagedRequest =
        new PublishRequest(
            request.caption(),
            staged,
            request.targets(),
            request.scheduledAt(),
            request.overrides());

    if (stagedRequest.isScheduled(Instant.now())) {
      UUID id = store.create(stagedRequest, PublicationStatus.SCHEDULED);
      log.info("Publication {} scheduled for {}", id, stagedRequest.scheduledAt());
      return new PublishResult(id, Map.of());
    }

    UUID id = store.create(stagedRequest, PublicationStatus.MEDIA_STAGED);
    return fanOut(id, stagedRequest);
  }

  /** Called by the scheduler once a scheduled publication comes due. */
  public PublishResult executeStaged(StagedPublication staged) {
    return fanOut(staged.id(), staged.request());
  }

  public Optional<PublicationView> status(UUID publicationId) {
    return store.get(publicationId);
  }

  public List<PublicationView> history(PublicationStatus status, Platform platform, int limit) {
    return store.list(status, platform, limit);
  }

  public boolean cancelScheduled(UUID publicationId) {
    return store.cancelScheduled(publicationId);
  }

  public List<StagedPublication> claimDue(Instant now, int max) {
    return store.claimDue(now, max);
  }

  private void validate(PublishRequest request) {
    List<String> violations = new ArrayList<>();
    if (request.targets().isEmpty()) {
      violations.add("no target platforms specified");
    }
    for (Platform target : request.targets()) {
      if (!publishers.containsKey(target)) {
        violations.add("no connector registered for " + target);
      }
    }
    violations.addAll(validator.validate(request.media(), request.targets()));
    if (!violations.isEmpty()) {
      throw new ValidationException(violations);
    }
  }

  private PublishResult fanOut(UUID id, PublishRequest request) {
    store.updateStatus(id, PublicationStatus.PUBLISHING);

    Map<Platform, CompletableFuture<PlatformOutcome>> futures = new EnumMap<>(Platform.class);
    for (Platform target : request.targets()) {
      SocialPublisher publisher = publishers.get(target);
      PublishCommand command =
          new PublishCommand(request.caption(), request.media(), request.overridesFor(target));
      futures.put(
          target,
          CompletableFuture.supplyAsync(
              () -> publishWithResilience(id, target, publisher, command), executor));
    }

    Map<Platform, PlatformOutcome> outcomes = new EnumMap<>(Platform.class);
    futures.forEach(
        (platform, future) -> {
          PlatformOutcome outcome = future.join();
          outcomes.put(platform, outcome);
          store.saveOutcome(id, platform, outcome);
        });

    PublishResult result = new PublishResult(id, outcomes);
    store.updateStatus(id, result.overallStatus());
    log.info("Publication {} finished with status {}", id, result.overallStatus());
    return result;
  }

  private PlatformOutcome publishWithResilience(
      UUID id, Platform platform, SocialPublisher publisher, PublishCommand command) {
    Retry retry = buildRetry(platform);
    RateLimiter limiter = rateLimiters.get(platform);
    AtomicInteger attemptCounter = new AtomicInteger();

    Supplier<PlatformOutcome> operation =
        () -> {
          int attemptNo = attemptCounter.incrementAndGet();
          Instant start = Instant.now();
          try {
            PlatformOutcome outcome = publisher.publish(command);
            store.recordAttempt(
                new PublicationAttempt(
                    id, platform, attemptNo, start, Instant.now(), null, describe(outcome)));
            return outcome;
          } catch (RuntimeException ex) {
            store.recordAttempt(
                new PublicationAttempt(
                    id, platform, attemptNo, start, Instant.now(), null, excerpt(ex.getMessage())));
            log.warn("Publish attempt {} to {} threw: {}", attemptNo, platform, ex.toString());
            throw ex;
          }
        };

    Supplier<PlatformOutcome> decorated = Retry.decorateSupplier(retry, operation);
    if (limiter != null) {
      decorated = RateLimiter.decorateSupplier(limiter, decorated);
    }

    try {
      return decorated.get();
    } catch (RuntimeException ex) {
      return PlatformOutcome.fail("CONNECTOR_EXCEPTION", excerpt(ex.getMessage()), false);
    }
  }

  private Retry buildRetry(Platform platform) {
    RetryConfig config =
        RetryConfig.<PlatformOutcome>custom()
            .maxAttempts(settings.maxAttempts())
            .intervalFunction(
                IntervalFunction.ofExponentialBackoff(settings.initialBackoff().toMillis(), 2.0))
            .retryOnResult(
                outcome ->
                    outcome instanceof PlatformOutcome.Failure failure && failure.retryable())
            .retryExceptions(RuntimeException.class)
            .build();
    return Retry.of("publish-" + platform, config);
  }

  private static String describe(PlatformOutcome outcome) {
    if (outcome instanceof PlatformOutcome.Success s) {
      return "SUCCESS postId=" + s.postId();
    }
    PlatformOutcome.Failure f = (PlatformOutcome.Failure) outcome;
    return "FAILURE code=" + f.errorCode() + " retryable=" + f.retryable();
  }

  private static String excerpt(String value) {
    if (value == null) {
      return null;
    }
    return value.length() <= EXCERPT_LIMIT ? value : value.substring(0, EXCERPT_LIMIT);
  }
}
