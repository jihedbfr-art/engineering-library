package dev.jihed.socialpub.app.config;

import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.SocialPublisher;
import dev.jihed.socialpub.core.PublicationService;
import dev.jihed.socialpub.core.ResilienceSettings;
import dev.jihed.socialpub.core.port.MediaStager;
import dev.jihed.socialpub.core.port.MediaValidator;
import dev.jihed.socialpub.core.port.PublicationStore;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the core orchestrator from the platform adapters discovered on the classpath. */
@Configuration
@EnableConfigurationProperties(OrchestrationProperties.class)
public class OrchestrationConfig {

  private static final Logger log = LoggerFactory.getLogger(OrchestrationConfig.class);

  /** Bounded, named pool for the per-platform fan-out. Java 17 has no virtual threads. */
  @Bean(destroyMethod = "shutdown")
  public ExecutorService publishExecutor() {
    AtomicInteger counter = new AtomicInteger();
    return Executors.newFixedThreadPool(
        8,
        runnable -> {
          Thread thread = new Thread(runnable, "publish-" + counter.incrementAndGet());
          thread.setDaemon(true);
          return thread;
        });
  }

  @Bean
  public ResilienceSettings resilienceSettings(OrchestrationProperties properties) {
    return new ResilienceSettings(
        properties.maxAttempts(), properties.initialBackoff(), properties.permitsPerSecond());
  }

  @Bean
  public PublicationService publicationService(
      List<SocialPublisher> publishers,
      MediaStager stager,
      MediaValidator validator,
      PublicationStore store,
      ResilienceSettings settings,
      ExecutorService publishExecutor) {
    Map<Platform, SocialPublisher> byPlatform = new EnumMap<>(Platform.class);
    for (SocialPublisher publisher : publishers) {
      SocialPublisher previous = byPlatform.put(publisher.platform(), publisher);
      if (previous != null) {
        log.warn("Two publishers registered for {}, keeping {}", publisher.platform(), publisher);
      }
    }
    log.info("Orchestrator wired with connectors: {}", byPlatform.keySet());
    return new PublicationService(byPlatform, stager, validator, store, settings, publishExecutor);
  }
}
