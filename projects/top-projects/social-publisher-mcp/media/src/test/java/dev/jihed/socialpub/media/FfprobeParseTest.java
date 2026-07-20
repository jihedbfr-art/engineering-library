package dev.jihed.socialpub.media;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FfprobeParseTest {

  @Test
  void parsesDurationAndResolution() {
    String json =
        """
        {
          "streams": [
            {"codec_type": "audio"},
            {"codec_type": "video", "width": 1080, "height": 1920}
          ],
          "format": {"duration": "12.500000"}
        }
        """;
    var probe = Ffprobe.parse(json).orElseThrow();
    assertThat(probe.durationMs()).isEqualTo(12500);
    assertThat(probe.width()).isEqualTo(1080);
    assertThat(probe.height()).isEqualTo(1920);
  }

  @Test
  void emptyOnGarbage() {
    assertThat(Ffprobe.parse("not json")).isEmpty();
  }

  @Test
  void emptyWhenNothingUseful() {
    assertThat(Ffprobe.parse("{\"format\":{}}")).isEmpty();
  }
}
