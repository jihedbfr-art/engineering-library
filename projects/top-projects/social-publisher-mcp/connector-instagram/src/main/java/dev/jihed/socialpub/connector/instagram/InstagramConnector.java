package dev.jihed.socialpub.connector.instagram;

import com.fasterxml.jackson.databind.JsonNode;
import dev.jihed.socialpub.api.CredentialProvider;
import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.MediaType;
import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.api.PublishCommand;
import dev.jihed.socialpub.api.SocialPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Publishes to Instagram via the Graph API three-step flow: create a media container, poll it until
 * {@code FINISHED}, then publish it. Videos are posted as Reels.
 */
public class InstagramConnector implements SocialPublisher {

  private static final Logger log = LoggerFactory.getLogger(InstagramConnector.class);

  private final InstagramProperties properties;
  private final CredentialProvider credentials;
  private final RestClient rest;

  public InstagramConnector(InstagramProperties properties, CredentialProvider credentials) {
    this.properties = properties;
    this.credentials = credentials;
    this.rest = RestClient.builder().baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Platform platform() {
    return Platform.INSTAGRAM;
  }

  @Override
  public PlatformOutcome publish(PublishCommand command) {
    if (command.media().isEmpty()) {
      return PlatformOutcome.fail("NO_MEDIA", "Instagram requires an image or video", false);
    }
    String token = credentials.require(Platform.INSTAGRAM);
    String user = properties.igUserId();
    MediaRef media = command.media().get(0);
    try {
      String containerId = createContainer(user, token, media, command.effectiveCaption());
      if (!awaitFinished(containerId, token)) {
        return PlatformOutcome.fail(
            "CONTAINER_NOT_READY", "container never reached FINISHED", true);
      }
      String mediaId = publishContainer(user, token, containerId);
      String permalink = fetchPermalink(mediaId, token);
      return PlatformOutcome.ok(mediaId, permalink);
    } catch (RestClientResponseException e) {
      boolean retryable = e.getStatusCode().is5xxServerError() || e.getStatusCode().value() == 429;
      log.warn("Instagram publish failed: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
      return PlatformOutcome.fail(
          "HTTP_" + e.getStatusCode().value(), excerpt(e.getResponseBodyAsString()), retryable);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return PlatformOutcome.fail("INTERRUPTED", "interrupted while polling", true);
    } catch (Exception e) {
      return PlatformOutcome.fail("IO", String.valueOf(e.getMessage()), true);
    }
  }

  private String createContainer(String user, String token, MediaRef media, String caption) {
    JsonNode response =
        rest.post()
            .uri(
                b -> {
                  var builder = b.path("/{user}/media").queryParam("caption", caption);
                  if (media.type() == MediaType.VIDEO) {
                    builder.queryParam("media_type", "REELS").queryParam("video_url", media.url());
                  } else {
                    builder.queryParam("image_url", media.url());
                  }
                  return builder.build(user);
                })
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .body(JsonNode.class);
    return response.path("id").asText();
  }

  private boolean awaitFinished(String containerId, String token) throws InterruptedException {
    for (int attempt = 0; attempt < properties.pollAttempts(); attempt++) {
      JsonNode status =
          rest.get()
              .uri(b -> b.path("/{id}").queryParam("fields", "status_code").build(containerId))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
              .retrieve()
              .body(JsonNode.class);
      String code = status.path("status_code").asText("");
      if ("FINISHED".equals(code)) {
        return true;
      }
      if ("ERROR".equals(code) || "EXPIRED".equals(code)) {
        return false;
      }
      Thread.sleep(2000);
    }
    return false;
  }

  private String publishContainer(String user, String token, String containerId) {
    JsonNode response =
        rest.post()
            .uri(
                b ->
                    b.path("/{user}/media_publish")
                        .queryParam("creation_id", containerId)
                        .build(user))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .body(JsonNode.class);
    return response.path("id").asText();
  }

  private String fetchPermalink(String mediaId, String token) {
    try {
      JsonNode response =
          rest.get()
              .uri(b -> b.path("/{id}").queryParam("fields", "permalink").build(mediaId))
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
              .retrieve()
              .body(JsonNode.class);
      return response.path("permalink").asText(null);
    } catch (Exception e) {
      log.debug("Could not fetch permalink for {}: {}", mediaId, e.getMessage());
      return null;
    }
  }

  private static String excerpt(String value) {
    if (value == null) {
      return null;
    }
    return value.length() <= 500 ? value : value.substring(0, 500);
  }
}
