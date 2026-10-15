package dev.jihed.socialpub.app;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.core.PublicationService;
import dev.jihed.socialpub.core.PublicationStatus;
import dev.jihed.socialpub.core.PublishRequest;
import dev.jihed.socialpub.core.PublishResult;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Boots the whole application against real Postgres and an S3-compatible test server, then drives a
 * publication through the MOCK connector end to end — the project's definition of done. The {@code
 * dev} profile activates the MOCK publisher.
 */
@SpringBootTest
@ActiveProfiles("dev")
@Testcontainers
class SocialPublisherEndToEndIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  static GenericContainer<?> s3 =
      new GenericContainer<>(DockerImageName.parse("adobe/s3mock:5.2.3"))
          .withExposedPorts(9090)
          .waitingFor(Wait.forHttp("/favicon.ico").forStatusCode(200));

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add(
        "media.minio.endpoint", () -> "http://" + s3.getHost() + ":" + s3.getMappedPort(9090));
    registry.add("media.minio.access-key", () -> "testuser");
    registry.add("media.minio.secret-key", () -> "testpass");
    // Push the scheduled poller far out so it doesn't fire against the container during teardown.
    registry.add("socialpub.scheduling.poll-interval-ms", () -> "3600000");
  }

  @Autowired private PublicationService publications;
  @Autowired private ToolCallbackProvider toolCallbackProvider;
  @Autowired private MinioClient minioClient;

  @Test
  void exposesTheFullToolset() {
    assertThat(toolCallbackProvider.getToolCallbacks()).hasSize(8);
  }

  @Test
  void publishesThroughTheMockConnectorEndToEnd() throws Exception {
    PublishRequest request =
        new PublishRequest(
            "hello from the integration test", List.of(), Set.of(Platform.MOCK), null, Map.of());

    PublishResult result = publications.publish(request);

    assertThat(result.overallStatus()).isEqualTo(PublicationStatus.PUBLISHED);
    assertThat(result.outcomes().get(Platform.MOCK).success()).isTrue();
    // and it is durably recorded
    assertThat(publications.status(result.publicationId()).orElseThrow().status())
        .isEqualTo(PublicationStatus.PUBLISHED);
    assertThat(minioClient.bucketExists(BucketExistsArgs.builder().bucket("media-staging").build()))
        .isTrue();
  }
}
