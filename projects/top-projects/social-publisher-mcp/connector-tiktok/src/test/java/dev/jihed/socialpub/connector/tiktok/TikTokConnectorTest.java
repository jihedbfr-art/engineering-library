package dev.jihed.socialpub.connector.tiktok;

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

class TikTokConnectorTest {

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

  private TikTokConnector connector(String mode) {
    TikTokProperties props = new TikTokProperties(server.url("/").toString(), mode, 5);
    return new TikTokConnector(props, platform -> Optional.of("tt-token"));
  }

  private static MockResponse json(String body) {
    return new MockResponse().setHeader("Content-Type", "application/json").setBody(body);
  }

  @Test
  void draftModeInitsToInboxAndPollsUntilComplete() throws Exception {
    server.enqueue(json("{\"data\":{\"publish_id\":\"pub123\"},\"error\":{\"code\":\"ok\"}}"));
    server.enqueue(
        json("{\"data\":{\"status\":\"SEND_TO_USER_INBOX\"},\"error\":{\"code\":\"ok\"}}"));

    PlatformOutcome outcome =
        connector("DRAFT")
            .publish(
                new PublishCommand(
                    "dance", List.of(MediaRef.ofUrl("https://cdn/v.mp4", MediaType.VIDEO)), null));

    assertThat(outcome).isInstanceOf(PlatformOutcome.Success.class);
    assertThat(((PlatformOutcome.Success) outcome).postId()).isEqualTo("pub123");

    RecordedRequest init = server.takeRequest();
    assertThat(init.getPath()).isEqualTo("/v2/post/publish/inbox/video/init/");
    assertThat(init.getBody().readUtf8()).contains("PULL_FROM_URL");
    assertThat(server.takeRequest().getPath()).isEqualTo("/v2/post/publish/status/fetch/");
  }

  @Test
  void directModeUsesPublishEndpointWithPostInfo() throws Exception {
    server.enqueue(json("{\"data\":{\"publish_id\":\"pub999\"},\"error\":{\"code\":\"ok\"}}"));
    server.enqueue(
        json("{\"data\":{\"status\":\"PUBLISH_COMPLETE\"},\"error\":{\"code\":\"ok\"}}"));

    connector("DIRECT")
        .publish(
            new PublishCommand(
                "hi", List.of(MediaRef.ofUrl("https://cdn/v.mp4", MediaType.VIDEO)), null));

    RecordedRequest init = server.takeRequest();
    assertThat(init.getPath()).isEqualTo("/v2/post/publish/video/init/");
    assertThat(init.getBody().readUtf8()).contains("post_info");
  }

  @Test
  void rejectsWhenNoVideo() {
    PlatformOutcome outcome = connector("DRAFT").publish(new PublishCommand("hi", List.of(), null));
    assertThat(((PlatformOutcome.Failure) outcome).errorCode()).isEqualTo("NO_MEDIA");
  }
}
