package dev.jihed.socialpub.connector.facebook;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.MediaType;
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

class FacebookConnectorTest {

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

  private FacebookConnector connector() {
    FacebookProperties props = new FacebookProperties(server.url("/").toString(), "222");
    return connectorWith(props);
  }

  private FacebookConnector connectorWith(FacebookProperties props) {
    return new FacebookConnector(props, platform -> Optional.of("page-token"));
  }

  @Test
  void postsPhotoByUrlAndUsesPostId() throws Exception {
    server.enqueue(
        new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("{\"id\":\"111\",\"post_id\":\"222_333\"}"));

    PlatformOutcome outcome =
        connector()
            .publish(
                new PublishCommand(
                    "a caption",
                    List.of(MediaRef.ofUrl("https://cdn/x.jpg", MediaType.IMAGE)),
                    null));

    assertThat(outcome).isInstanceOf(PlatformOutcome.Success.class);
    assertThat(((PlatformOutcome.Success) outcome).postId()).isEqualTo("222_333");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).startsWith("/222/photos");
    assertThat(request.getPath()).contains("url=");
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer page-token");
  }

  @Test
  void textOnlyGoesToFeed() throws Exception {
    server.enqueue(
        new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("{\"id\":\"222_444\"}"));

    connector().publish(new PublishCommand("just text", List.of(), null));

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).startsWith("/222/feed");
    assertThat(request.getPath()).contains("message=");
  }
}
