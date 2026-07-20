package dev.jihed.socialpub.connector.linkedin;

import dev.jihed.socialpub.api.CredentialProvider;
import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.api.PublishCommand;
import dev.jihed.socialpub.api.SocialPublisher;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Posts to LinkedIn via the versioned Posts API ({@code /rest/posts}). v1 publishes the caption as
 * a text share; the created post URN comes back in the {@code x-restli-id} response header.
 */
public class LinkedInConnector implements SocialPublisher {

  private static final Logger log = LoggerFactory.getLogger(LinkedInConnector.class);

  private final LinkedInProperties properties;
  private final CredentialProvider credentials;
  private final RestClient rest;

  public LinkedInConnector(LinkedInProperties properties, CredentialProvider credentials) {
    this.properties = properties;
    this.credentials = credentials;
    this.rest = RestClient.builder().baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Platform platform() {
    return Platform.LINKEDIN;
  }

  @Override
  public PlatformOutcome publish(PublishCommand command) {
    String token = credentials.require(Platform.LINKEDIN);
    Map<String, Object> body =
        Map.of(
            "author",
            properties.authorUrn(),
            "commentary",
            command.effectiveCaption(),
            "visibility",
            "PUBLIC",
            "distribution",
            Map.of(
                "feedDistribution", "MAIN_FEED",
                "targetEntities", List.of(),
                "thirdPartyDistributionChannels", List.of()),
            "lifecycleState",
            "PUBLISHED",
            "isReshareDisabledByAuthor",
            false);
    try {
      ResponseEntity<Void> response =
          rest.post()
              .uri("/rest/posts")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
              .header("LinkedIn-Version", properties.version())
              .header("X-Restli-Protocol-Version", "2.0.0")
              .contentType(MediaType.APPLICATION_JSON)
              .body(body)
              .retrieve()
              .toBodilessEntity();
      String urn = response.getHeaders().getFirst("x-restli-id");
      String permalink = "https://www.linkedin.com/feed/update/" + urn;
      return PlatformOutcome.ok(urn, permalink);
    } catch (RestClientResponseException e) {
      boolean retryable = e.getStatusCode().is5xxServerError() || e.getStatusCode().value() == 429;
      log.warn("LinkedIn post failed: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
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
