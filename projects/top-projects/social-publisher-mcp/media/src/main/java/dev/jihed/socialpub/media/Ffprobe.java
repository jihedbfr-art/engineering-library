package dev.jihed.socialpub.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around the {@code ffprobe} CLI to read a video's duration and resolution. If the
 * binary is not installed the probe returns empty and the caller skips AV checks — this never fails
 * the pipeline, matching the spec's "WARN and continue" rule.
 */
@Component
public class Ffprobe {

  private static final Logger log = LoggerFactory.getLogger(Ffprobe.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public record Probe(Long durationMs, Integer width, Integer height) {}

  public Optional<Probe> probe(Path file) {
    try {
      Process process =
          new ProcessBuilder(
                  "ffprobe",
                  "-v",
                  "quiet",
                  "-print_format",
                  "json",
                  "-show_format",
                  "-show_streams",
                  file.toString())
              .redirectErrorStream(true)
              .start();
      String output = new String(process.getInputStream().readAllBytes());
      if (!process.waitFor(30, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        log.warn("ffprobe timed out for {}", file);
        return Optional.empty();
      }
      if (process.exitValue() != 0) {
        log.warn("ffprobe exited {} for {}", process.exitValue(), file);
        return Optional.empty();
      }
      return parse(output);
    } catch (IOException e) {
      log.warn("ffprobe not available, skipping AV metadata: {}", e.getMessage());
      return Optional.empty();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return Optional.empty();
    }
  }

  /** Parse ffprobe JSON. Package-visible so the parsing is unit-testable without the binary. */
  static Optional<Probe> parse(String json) {
    try {
      JsonNode root = MAPPER.readTree(json);
      Long durationMs = null;
      JsonNode duration = root.path("format").path("duration");
      if (duration.isTextual() || duration.isNumber()) {
        durationMs = Math.round(Double.parseDouble(duration.asText()) * 1000);
      }
      Integer width = null;
      Integer height = null;
      for (JsonNode stream : root.path("streams")) {
        if ("video".equals(stream.path("codec_type").asText())) {
          width = stream.path("width").isMissingNode() ? null : stream.path("width").asInt();
          height = stream.path("height").isMissingNode() ? null : stream.path("height").asInt();
          break;
        }
      }
      if (durationMs == null && width == null) {
        return Optional.empty();
      }
      return Optional.of(new Probe(durationMs, width, height));
    } catch (RuntimeException | IOException e) {
      return Optional.empty();
    }
  }
}
