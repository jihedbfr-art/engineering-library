package dev.jihed.socialpub.connector.tiktok;

import com.fasterxml.jackson.databind.JsonNode;
import dev.jihed.socialpub.api.CredentialProvider;
import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.api.PublishCommand;
import dev.jihed.socialpub.api.SocialPublisher;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Publishes a video to TikTok via the Content Posting API using {@code PULL_FROM_URL}. It
 * initializes the post, then polls the status endpoint until it completes. DRAFT mode sends the
 * video to the user's inbox — the only option for apps that haven't passed audit.
 */
public class TikTokConnector implements SocialPublisher {

  private static final Logger log = LoggerFactory.getLogger(TikTokConnector.class);

  private final TikTokProperties properties;
  private final CredentialProvider credentials;
  private final RestClient rest;

  public TikTokConnector(TikTokProperties properties, CredentialProvider credentials) {
    this.properties = properties;
    this.credentials = credentials;
    this.rest = RestClient.builder().baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Platform platform() {
    return Platform.TIKTOK;
  }

  @Override
  public PlatformOutcome publish(PublishCommand command) {
    if (command.media().isEmpty()) {
      return PlatformOutcome.fail("NO_MEDIA", "TikTok requires a video", false);
    }
    MediaRef video = command.media().get(0);
    String token = credentials.require(Platform.TIKTOK);
    try {
      JsonNode init = initPost(token, video, command.effectiveCaption());
      String initError = init.path("error").path("code").asText("ok");
      if (!isOk(initError)) {
        return PlatformOutcome.fail(
            "INIT_" + initError, init.path("error").path("message").asText(), false);
      }
      String publishId = init.path("data").path("publish_id").asText(null);
      if (publishId == null) {
        return PlatformOutcome.fail("NO_PUBLISH_ID", "no publish_id returned", false);
      }
      return awaitCompletion(token, publishId);
    } catch (RestClientResponseException e) {
      boolean retryable = e.getStatusCode().is5xxServerError() || e.getStatusCode().value() == 429;
      log.warn("TikTok publish failed: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
      return PlatformOutcome.fail(
          "HTTP_" + e.getStatusCode().value(), excerpt(e.getResponseBodyAsString()), retryable);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return PlatformOutcome.fail("INTERRUPTED", "interrupted while polling", true);
    } catch (Exception e) {
      return PlatformOutcome.fail("IO", String.valueOf(e.getMessage()), true);
    }
  }

  private JsonNode initPost(String token, MediaRef video, String caption) {
    String path =
        properties.direct() ? "/v2/post/publish/video/init/" : "/v2/post/publish/inbox/video/init/";
    Map<String, Object> sourceInfo = Map.of("source", "PULL_FROM_URL", "video_url", video.url());
    Map<String, Object> body =
        properties.direct()
            ? Map.of(
                "post_info",
                Map.of("title", caption, "privacy_level", "SELF_ONLY"),
                "source_info",
                sourceInfo)
            : Map.of("source_info", sourceInfo);
    return rest.post()
        .uri(path)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(JsonNode.class);
  }

  private PlatformOutcome awaitCompletion(String token, String publishId)
      throws InterruptedException {
    for (int attempt = 0; attempt < properties.pollAttempts(); attempt++) {
      JsonNode status =
          rest.post()
              .uri("/v2/post/publish/status/fetch/")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .body(Map.of("publish_id", publishId))
              .retrieve()
              .body(JsonNode.class);
      String state = status.path("data").path("status").asText("");
      if ("PUBLISH_COMPLETE".equals(state) || "SEND_TO_USER_INBOX".equals(state)) {
        return PlatformOutcome.ok(publishId, null);
      }
      if ("FAILED".equals(state)) {
        return PlatformOutcome.fail("PUBLISH_FAILED", "TikTok reported FAILED", true);
      }
      Thread.sleep(2000);
    }
    return PlatformOutcome.fail("TIMEOUT", "status never completed", true);
  }

  private static boolean isOk(String code) {
    return code == null || code.isEmpty() || "ok".equalsIgnoreCase(code);
  }

  private static String excerpt(String value) {
    if (value == null) {
      return null;
    }
    return value.length() <= 500 ? value : value.substring(0, 500);
  }
}
