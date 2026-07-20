package dev.jihed.socialpub.connector.facebook;

import com.fasterxml.jackson.databind.JsonNode;
import dev.jihed.socialpub.api.CredentialProvider;
import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.MediaType;
import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.api.PublishCommand;
import dev.jihed.socialpub.api.SocialPublisher;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

/**
 * Publishes to a Facebook Page. Text goes to {@code /{pageId}/feed}, an image to {@code /photos}
 * (by URL) and a video to {@code /videos} (by {@code file_url}). The Page access token is sent as a
 * bearer, which the Graph API accepts.
 */
public class FacebookConnector implements SocialPublisher {

  private static final Logger log = LoggerFactory.getLogger(FacebookConnector.class);

  private final FacebookProperties properties;
  private final CredentialProvider credentials;
  private final RestClient rest;

  public FacebookConnector(FacebookProperties properties, CredentialProvider credentials) {
    this.properties = properties;
    this.credentials = credentials;
    this.rest = RestClient.builder().baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Platform platform() {
    return Platform.FACEBOOK;
  }

  @Override
  public PlatformOutcome publish(PublishCommand command) {
    String token = credentials.require(Platform.FACEBOOK);
    String caption = command.effectiveCaption();
    MediaRef first = command.media().isEmpty() ? null : command.media().get(0);
    try {
      Function<UriBuilder, java.net.URI> uri = uriFor(first, caption);
      JsonNode response =
          rest.post()
              .uri(uri)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
              .retrieve()
              .body(JsonNode.class);
      String id =
          response == null
              ? null
              : response.hasNonNull("post_id")
                  ? response.get("post_id").asText()
                  : response.path("id").asText(null);
      if (id == null) {
        return PlatformOutcome.fail("NO_ID", "Facebook response had no id", false);
      }
      return PlatformOutcome.ok(id, "https://www.facebook.com/" + id);
    } catch (RestClientResponseException e) {
      boolean retryable = e.getStatusCode().is5xxServerError() || e.getStatusCode().value() == 429;
      log.warn("Facebook post failed: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
      return PlatformOutcome.fail(
          "HTTP_" + e.getStatusCode().value(), excerpt(e.getResponseBodyAsString()), retryable);
    } catch (Exception e) {
      return PlatformOutcome.fail("IO", String.valueOf(e.getMessage()), true);
    }
  }

  private Function<UriBuilder, java.net.URI> uriFor(MediaRef media, String caption) {
    String page = properties.pageId();
    if (media == null) {
      return b -> b.path("/{page}/feed").queryParam("message", caption).build(page);
    }
    if (media.type() == MediaType.VIDEO) {
      return b ->
          b.path("/{page}/videos")
              .queryParam("file_url", media.url())
              .queryParam("description", caption)
              .build(page);
    }
    return b ->
        b.path("/{page}/photos")
            .queryParam("url", media.url())
            .queryParam("caption", caption)
            .build(page);
  }

  private static String excerpt(String value) {
    if (value == null) {
      return null;
    }
    return value.length() <= 500 ? value : value.substring(0, 500);
  }
}
