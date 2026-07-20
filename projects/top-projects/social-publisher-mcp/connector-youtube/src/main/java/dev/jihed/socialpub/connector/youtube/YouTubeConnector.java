package dev.jihed.socialpub.connector.youtube;

import com.fasterxml.jackson.databind.JsonNode;
import dev.jihed.socialpub.api.CredentialProvider;
import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.api.PublishCommand;
import dev.jihed.socialpub.api.SocialPublisher;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Uploads a Short to YouTube with the Data API v3 resumable flow: start a session with the video
 * metadata, then PUT the video bytes to the session URL returned in the {@code Location} header.
 */
public class YouTubeConnector implements SocialPublisher {

  private static final Logger log = LoggerFactory.getLogger(YouTubeConnector.class);

  private final YouTubeProperties properties;
  private final CredentialProvider credentials;
  private final RestClient rest;
  private final HttpClient http = HttpClient.newHttpClient();

  public YouTubeConnector(YouTubeProperties properties, CredentialProvider credentials) {
    this.properties = properties;
    this.credentials = credentials;
    this.rest = RestClient.builder().baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Platform platform() {
    return Platform.YOUTUBE;
  }

  @Override
  public PlatformOutcome publish(PublishCommand command) {
    if (command.media().isEmpty()) {
      return PlatformOutcome.fail("NO_MEDIA", "YouTube requires a video", false);
    }
    MediaRef video = command.media().get(0);
    String token = credentials.require(Platform.YOUTUBE);
    String title = title(command);
    Map<String, Object> metadata =
        Map.of(
            "snippet", Map.of("title", title, "description", command.effectiveCaption()),
            "status", Map.of("privacyStatus", properties.privacyStatus()));
    try {
      ResponseEntity<Void> session =
          rest.post()
              .uri(
                  b ->
                      b.path("/upload/youtube/v3/videos")
                          .queryParam("uploadType", "resumable")
                          .queryParam("part", "snippet,status")
                          .build())
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .body(metadata)
              .retrieve()
              .toBodilessEntity();
      String uploadUrl = session.getHeaders().getFirst("Location");
      if (uploadUrl == null) {
        return PlatformOutcome.fail("NO_SESSION", "no resumable Location header", true);
      }

      byte[] bytes = download(video.url());
      JsonNode result = uploadBytes(uploadUrl, bytes);
      String id = result.path("id").asText(null);
      if (id == null) {
        return PlatformOutcome.fail("NO_ID", "upload response had no video id", false);
      }
      return PlatformOutcome.ok(id, "https://youtube.com/shorts/" + id);
    } catch (RestClientResponseException e) {
      boolean retryable = e.getStatusCode().is5xxServerError() || e.getStatusCode().value() == 429;
      log.warn("YouTube upload failed: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
      return PlatformOutcome.fail(
          "HTTP_" + e.getStatusCode().value(), excerpt(e.getResponseBodyAsString()), retryable);
    } catch (Exception e) {
      return PlatformOutcome.fail("IO", String.valueOf(e.getMessage()), true);
    }
  }

  private String title(PublishCommand command) {
    String base =
        command.overrides() != null && command.overrides().title() != null
            ? command.overrides().title()
            : firstLine(command.effectiveCaption());
    return Boolean.TRUE.equals(properties.shortsMode()) ? base + " #Shorts" : base;
  }

  private JsonNode uploadBytes(String uploadUrl, byte[] bytes) throws Exception {
    RestClient absolute = RestClient.create();
    return absolute
        .put()
        .uri(URI.create(uploadUrl))
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(bytes)
        .retrieve()
        .body(JsonNode.class);
  }

  private byte[] download(String url) throws Exception {
    HttpResponse<byte[]> response =
        http.send(
            HttpRequest.newBuilder(URI.create(url)).GET().build(),
            HttpResponse.BodyHandlers.ofByteArray());
    if (response.statusCode() / 100 != 2) {
      throw new IllegalStateException("source download returned " + response.statusCode());
    }
    return response.body();
  }

  private static String firstLine(String caption) {
    if (caption == null || caption.isBlank()) {
      return "Untitled";
    }
    int nl = caption.indexOf('\n');
    String line = nl > 0 ? caption.substring(0, nl) : caption;
    return line.length() > 90 ? line.substring(0, 90) : line;
  }

  private static String excerpt(String value) {
    if (value == null) {
      return null;
    }
    return value.length() <= 500 ? value : value.substring(0, 500);
  }
}
