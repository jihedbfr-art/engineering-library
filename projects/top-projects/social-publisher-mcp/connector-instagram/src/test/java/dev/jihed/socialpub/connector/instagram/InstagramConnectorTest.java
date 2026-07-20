package dev.jihed.socialpub.connector.instagram;

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

class InstagramConnectorTest {

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

  private InstagramConnector connector() {
    InstagramProperties props = new InstagramProperties(server.url("/").toString(), "ig-1", 5);
    return new InstagramConnector(props, platform -> Optional.of("ig-token"));
  }

  private static MockResponse json(String body) {
    return new MockResponse().setHeader("Content-Type", "application/json").setBody(body);
  }

  @Test
  void runsContainerCreatePollPublishFlow() throws Exception {
    server.enqueue(json("{\"id\":\"container123\"}"));
    server.enqueue(json("{\"status_code\":\"FINISHED\"}"));
    server.enqueue(json("{\"id\":\"media999\"}"));
    server.enqueue(json("{\"permalink\":\"https://www.instagram.com/p/abc/\"}"));

    PlatformOutcome outcome =
        connector()
            .publish(
                new PublishCommand(
                    "sunset", List.of(MediaRef.ofUrl("https://cdn/p.jpg", MediaType.IMAGE)), null));

    assertThat(outcome).isInstanceOf(PlatformOutcome.Success.class);
    PlatformOutcome.Success success = (PlatformOutcome.Success) outcome;
    assertThat(success.postId()).isEqualTo("media999");
    assertThat(success.permalink()).isEqualTo("https://www.instagram.com/p/abc/");

    RecordedRequest create = server.takeRequest();
    assertThat(create.getPath()).startsWith("/ig-1/media");
    assertThat(create.getPath()).contains("image_url=");
    assertThat(server.takeRequest().getPath()).contains("status_code");
    assertThat(server.takeRequest().getPath()).startsWith("/ig-1/media_publish");
  }

  @Test
  void rejectsWhenNoMedia() {
    PlatformOutcome outcome = connector().publish(new PublishCommand("text", List.of(), null));
    assertThat(outcome).isInstanceOf(PlatformOutcome.Failure.class);
    assertThat(((PlatformOutcome.Failure) outcome).errorCode()).isEqualTo("NO_MEDIA");
  }
}
