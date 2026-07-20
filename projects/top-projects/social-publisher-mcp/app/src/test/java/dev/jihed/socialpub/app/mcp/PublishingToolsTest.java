package dev.jihed.socialpub.app.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.app.credentials.CredentialService;
import dev.jihed.socialpub.core.PublicationService;
import dev.jihed.socialpub.core.PublishResult;
import dev.jihed.socialpub.core.ValidationException;
import dev.jihed.socialpub.core.port.MediaValidator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PublishingToolsTest {

  private final PublicationService service = mock(PublicationService.class);
  private final CredentialService credentials = mock(CredentialService.class);
  private final MediaValidator validator = mock(MediaValidator.class);
  private final PublishingTools tools = new PublishingTools(service, credentials, validator);

  @Test
  void publishPost_mapsPerPlatformOutcomes() {
    UUID id = UUID.randomUUID();
    when(service.publish(any()))
        .thenReturn(
            new PublishResult(
                id,
                Map.of(
                    Platform.LINKEDIN, PlatformOutcome.ok("li-1", "https://li/1"),
                    Platform.X, PlatformOutcome.fail("AUTH", "bad token", false))));

    var result =
        tools.publishPost(
            "hello", List.of("https://x/a.jpg"), List.of("LINKEDIN", "X"), null, null);

    assertThat(result.publicationId()).isEqualTo(id.toString());
    assertThat(result.status()).isEqualTo("PARTIALLY_PUBLISHED");
    assertThat(result.outcomes()).hasSize(2);
    assertThat(result.outcomes())
        .anySatisfy(
            o -> {
              if (o.platform().equals("LINKEDIN")) {
                assertThat(o.success()).isTrue();
                assertThat(o.permalink()).isEqualTo("https://li/1");
              }
            });
  }

  @Test
  void publishPost_validationFailure_returnsRejected() {
    when(service.publish(any())).thenThrow(new ValidationException(List.of("no target platforms")));
    var result = tools.publishPost("hi", null, List.of(), null, null);
    assertThat(result.status()).isEqualTo("REJECTED");
    assertThat(result.note()).contains("no target platforms");
  }

  @Test
  void suggestHashtags_extractsKeywords() {
    var tags =
        tools.suggestHashtags("Launching our new payments platform for payments teams", "X", 3);
    assertThat(tags).isNotEmpty();
    assertThat(tags).allMatch(t -> t.startsWith("#"));
    assertThat(tags).contains("#payments"); // appears twice, ranks first
  }

  @Test
  void getPlatformLimits_reportsXVideoCap() {
    var limits = tools.getPlatformLimits("x");
    assertThat(limits.platform()).isEqualTo("X");
    assertThat(limits.videosAllowed()).isTrue();
    assertThat(limits.maxVideoSeconds()).isEqualTo(140);
  }

  @Test
  void getPlatformLimits_tiktokRejectsImages() {
    var limits = tools.getPlatformLimits("TIKTOK");
    assertThat(limits.imagesAllowed()).isFalse();
  }

  @Test
  void unknownPlatform_isRejectedClearly() {
    org.assertj.core.api.Assertions.assertThatThrownBy(() -> tools.getPlatformLimits("myspace"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown platform");
  }
}
