package dev.jihed.socialpub.media;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.MediaType;
import dev.jihed.socialpub.api.Platform;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MediaValidatorImplTest {

  private final MediaValidatorImpl validator = new MediaValidatorImpl();

  private static MediaRef image(String mime, long bytes) {
    return new MediaRef("https://x/y", "k", MediaType.IMAGE, mime, bytes, null);
  }

  private static MediaRef video(String mime, long bytes, long durationMs) {
    return new MediaRef("https://x/y", "k", MediaType.VIDEO, mime, bytes, durationMs);
  }

  @Test
  void validImageForX_hasNoViolations() {
    var violations =
        validator.validate(List.of(image("image/jpeg", 2_000_000)), Set.of(Platform.X));
    assertThat(violations).isEmpty();
  }

  @Test
  void oversizeImageForX_isFlagged() {
    var violations =
        validator.validate(List.of(image("image/jpeg", 9_000_000)), Set.of(Platform.X));
    assertThat(violations).anyMatch(v -> v.contains("image exceeds"));
  }

  @Test
  void imageToTikTok_isRejected() {
    var violations =
        validator.validate(List.of(image("image/jpeg", 1000)), Set.of(Platform.TIKTOK));
    assertThat(violations).anyMatch(v -> v.contains("does not accept images"));
  }

  @Test
  void longVideoForX_isFlagged() {
    var violations =
        validator.validate(List.of(video("video/mp4", 1000, 200_000)), Set.of(Platform.X));
    assertThat(violations).anyMatch(v -> v.contains("too long"));
  }

  @Test
  void wrongVideoTypeForLinkedIn_isFlagged() {
    var violations =
        validator.validate(List.of(video("video/webm", 1000, 1000)), Set.of(Platform.LINKEDIN));
    assertThat(violations).anyMatch(v -> v.contains("rejects video type"));
  }

  @Test
  void unknownFields_areNotChecked() {
    MediaRef beforeStaging = MediaRef.ofUrl("https://x/y", MediaType.IMAGE);
    assertThat(validator.validate(List.of(beforeStaging), Set.of(Platform.X))).isEmpty();
  }

  @Test
  void mockPlatform_acceptsAnything() {
    var violations =
        validator.validate(
            List.of(video("video/mp4", 9_000_000_000L, 99_999_000)), Set.of(Platform.MOCK));
    assertThat(violations).isEmpty();
  }
}
