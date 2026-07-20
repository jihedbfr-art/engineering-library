package dev.jihed.socialpub.connector.linkedin;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.api.PublishCommand;
import java.util.List;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LinkedInConnectorTest {

  private MockWebServer server;

  @BeforeEach
  void start() throws Exception {
    server = new MockWebServer();
    server.start();
  }

  @AfterEach
  void stop() throws Exception {
    server.shutdown();
  }

  private LinkedInConnector connector() {
    LinkedInProperties props =
        new LinkedInProperties(server.url("/").toString(), "202401", "PERSON", "abc123");
    return new LinkedInConnector(props, platform -> Optional.of("test-token"));
  }

  @Test
  void publishesTextShareAndReadsUrnFromHeader() throws Exception {
    server.enqueue(
        new MockResponse().setResponseCode(201).setHeader("x-restli-id", "urn:li:share:999"));

    PlatformOutcome outcome =
        connector().publish(new PublishCommand("hello world", List.of(), null));

    assertThat(outcome).isInstanceOf(PlatformOutcome.Success.class);
    PlatformOutcome.Success success = (PlatformOutcome.Success) outcome;
    assertThat(success.postId()).isEqualTo("urn:li:share:999");
    assertThat(success.permalink()).contains("urn:li:share:999");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/rest/posts");
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-token");
    assertThat(request.getHeader("LinkedIn-Version")).isEqualTo("202401");
    String body = request.getBody().readUtf8();
    assertThat(body).contains("\"commentary\":\"hello world\"");
    assertThat(body).contains("urn:li:person:abc123");
  }

  @Test
  void mapsClientErrorToNonRetryableFailure() {
    server.enqueue(new MockResponse().setResponseCode(422).setBody("{\"message\":\"bad author\"}"));

    PlatformOutcome outcome = connector().publish(new PublishCommand("x", List.of(), null));

    assertThat(outcome).isInstanceOf(PlatformOutcome.Failure.class);
    PlatformOutcome.Failure failure = (PlatformOutcome.Failure) outcome;
    assertThat(failure.errorCode()).isEqualTo("HTTP_422");
    assertThat(failure.retryable()).isFalse();
  }

  @Test
  void platformIsLinkedIn() {
    assertThat(connector().platform()).isEqualTo(Platform.LINKEDIN);
  }
}
