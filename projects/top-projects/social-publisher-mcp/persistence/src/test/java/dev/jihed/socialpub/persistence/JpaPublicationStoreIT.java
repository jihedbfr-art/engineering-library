package dev.jihed.socialpub.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.MediaType;
import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.api.PlatformOverrides;
import dev.jihed.socialpub.core.PublicationStatus;
import dev.jihed.socialpub.core.PublicationView;
import dev.jihed.socialpub.core.PublishRequest;
import dev.jihed.socialpub.core.port.StagedPublication;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaPublicationStore.class)
@Testcontainers
class JpaPublicationStoreIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void datasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.enabled", () -> true);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
  }

  @Autowired private JpaPublicationStore store;

  private PublishRequest request(Set<Platform> targets, Instant scheduledAt) {
    return new PublishRequest(
        "a caption",
        List.of(MediaRef.ofUrl("https://example.com/pic.jpg", MediaType.IMAGE)),
        targets,
        scheduledAt,
        Map.of(Platform.X, new PlatformOverrides("x caption", List.of("#hi"), null)));
  }

  @Test
  void create_thenGet_roundTrips() {
    UUID id =
        store.create(
            request(Set.of(Platform.LINKEDIN, Platform.X), null), PublicationStatus.MEDIA_STAGED);

    PublicationView view = store.get(id).orElseThrow();
    assertThat(view.caption()).isEqualTo("a caption");
    assertThat(view.status()).isEqualTo(PublicationStatus.MEDIA_STAGED);
    assertThat(view.targets()).hasSize(2);
    assertThat(view.targets())
        .allSatisfy(t -> assertThat(t.status()).isEqualTo(PublicationStatus.PENDING));
  }

  @Test
  void saveOutcome_updatesTarget() {
    UUID id = store.create(request(Set.of(Platform.LINKEDIN), null), PublicationStatus.PUBLISHING);
    store.saveOutcome(id, Platform.LINKEDIN, PlatformOutcome.ok("urn:li:123", "https://li/123"));

    PublicationView.Target target = store.get(id).orElseThrow().targets().get(0);
    assertThat(target.status()).isEqualTo(PublicationStatus.PUBLISHED);
    assertThat(target.externalPostId()).isEqualTo("urn:li:123");
    assertThat(target.permalink()).isEqualTo("https://li/123");
  }

  @Test
  void saveOutcome_failure_recordsError() {
    UUID id = store.create(request(Set.of(Platform.X), null), PublicationStatus.PUBLISHING);
    store.saveOutcome(id, Platform.X, PlatformOutcome.fail("AUTH", "token expired", false));

    PublicationView.Target target = store.get(id).orElseThrow().targets().get(0);
    assertThat(target.status()).isEqualTo(PublicationStatus.FAILED);
    assertThat(target.errorCode()).isEqualTo("AUTH");
  }

  @Test
  void claimDue_returnsScheduledAndReconstructsRequest() {
    UUID id =
        store.create(
            request(Set.of(Platform.X), Instant.now().minusSeconds(5)),
            PublicationStatus.SCHEDULED);

    List<StagedPublication> due = store.claimDue(Instant.now(), 10);

    assertThat(due).extracting(StagedPublication::id).contains(id);
    StagedPublication claimed =
        due.stream().filter(s -> s.id().equals(id)).findFirst().orElseThrow();
    assertThat(claimed.request().targets()).containsExactly(Platform.X);
    assertThat(claimed.request().overridesFor(Platform.X).caption()).isEqualTo("x caption");
    assertThat(store.get(id).orElseThrow().status()).isEqualTo(PublicationStatus.PUBLISHING);
  }

  @Test
  void cancelScheduled_flipsOnlyWhenScheduled() {
    UUID scheduled =
        store.create(
            request(Set.of(Platform.X), Instant.now().plusSeconds(3600)),
            PublicationStatus.SCHEDULED);
    assertThat(store.cancelScheduled(scheduled)).isTrue();
    assertThat(store.get(scheduled).orElseThrow().status()).isEqualTo(PublicationStatus.CANCELLED);

    UUID published = store.create(request(Set.of(Platform.X), null), PublicationStatus.PUBLISHED);
    assertThat(store.cancelScheduled(published)).isFalse();
  }
}
