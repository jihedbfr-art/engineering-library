package dev.jihed.socialpub.connector.x;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.api.PublishCommand;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XConnectorTest {

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

  private XConnector connector() {
    XProperties props = new XProperties(server.url("/").toString(), null, null, null, null);
    return new XConnector(props, platform -> Optional.of("oauth2-token"));
  }

  private static String fixture(String name) throws Exception {
    try (var in = XConnectorTest.class.getResourceAsStream("/fixtures/" + name)) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  @Test
  void postsTextTweetAndParsesId() throws Exception {
    server.enqueue(
        new MockResponse()
            .setResponseCode(201)
            .setHeader("Content-Type", "application/json")
            .setBody(fixture("create-tweet.json")));

    PlatformOutcome outcome = connector().publish(new PublishCommand("hello", List.of(), null));

    assertThat(outcome).isInstanceOf(PlatformOutcome.Success.class);
    PlatformOutcome.Success success = (PlatformOutcome.Success) outcome;
    assertThat(success.postId()).isEqualTo("1789000000000000001");
    assertThat(success.permalink()).endsWith("/1789000000000000001");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/2/tweets");
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer oauth2-token");
    assertThat(request.getBody().readUtf8()).contains("\"text\":\"hello\"");
  }

  @Test
  void rateLimitIsRetryable() {
    server.enqueue(
        new MockResponse().setResponseCode(429).setBody("{\"title\":\"Too Many Requests\"}"));
    PlatformOutcome outcome = connector().publish(new PublishCommand("x", List.of(), null));
    assertThat(outcome).isInstanceOf(PlatformOutcome.Failure.class);
    assertThat(((PlatformOutcome.Failure) outcome).retryable()).isTrue();
  }
}
