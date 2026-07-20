package dev.jihed.socialpub.connector.x;

import com.fasterxml.jackson.databind.JsonNode;
import dev.jihed.socialpub.api.CredentialProvider;
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
 * Posts to X with API v2 ({@code POST /2/tweets}) using an OAuth2 user token. Media upload goes
 * through the v1.1 chunked endpoint with OAuth1 signing and is tracked as a roadmap item; v1 posts
 * the caption as a text tweet.
 */
public class XConnector implements SocialPublisher {

  private static final Logger log = LoggerFactory.getLogger(XConnector.class);

  private final CredentialProvider credentials;
  private final RestClient rest;

  public XConnector(XProperties properties, CredentialProvider credentials) {
    this.credentials = credentials;
    this.rest = RestClient.builder().baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Platform platform() {
    return Platform.X;
  }

  @Override
  public PlatformOutcome publish(PublishCommand command) {
    String token = credentials.require(Platform.X);
    try {
      JsonNode response =
          rest.post()
              .uri("/2/tweets")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .body(Map.of("text", command.effectiveCaption()))
              .retrieve()
              .body(JsonNode.class);
      String id = response == null ? null : response.path("data").path("id").asText(null);
      if (id == null) {
        return PlatformOutcome.fail("NO_ID", "X response had no tweet id", false);
      }
      return PlatformOutcome.ok(id, "https://x.com/i/web/status/" + id);
    } catch (RestClientResponseException e) {
      boolean retryable = e.getStatusCode().is5xxServerError() || e.getStatusCode().value() == 429;
      log.warn("X tweet failed: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
      return PlatformOutcome.fail(
          "HTTP_" + e.getStatusCode().value(), excerpt(e.getResponseBodyAsString()), retryable);
    } catch (Exception e) {
      return PlatformOutcome.fail("IO", String.valueOf(e.getMessage()), true);
    }
  }

  private static String excerpt(String value) {
    if (value == null) {
      return null;
    }
    return value.length() <= 500 ? value : value.substring(0, 500);
  }
}
