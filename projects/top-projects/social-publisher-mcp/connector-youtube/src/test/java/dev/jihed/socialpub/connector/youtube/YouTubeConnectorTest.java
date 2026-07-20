package dev.jihed.socialpub.connector.youtube;

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

class YouTubeConnectorTest {

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

  private YouTubeConnector connector() {
    YouTubeProperties props = new YouTubeProperties(server.url("/").toString(), true, "unlisted");
    return new YouTubeConnector(props, platform -> Optional.of("google-token"));
  }

  @Test
  void resumableUploadInitThenPut() throws Exception {
    // 1. session init returns the upload URL in Location
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setHeader("Location", server.url("/session-up").toString()));
    // 2. the connector downloads the source video
    server.enqueue(new MockResponse().setResponseCode(200).setBody("fake-video-bytes"));
    // 3. the PUT upload returns the created video
    server.enqueue(
        new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("{\"id\":\"vid123\",\"snippet\":{\"title\":\"My cool short #Shorts\"}}"));

    PlatformOutcome outcome =
        connector()
            .publish(
                new PublishCommand(
                    "My cool short",
                    List.of(MediaRef.ofUrl(server.url("/video.mp4").toString(), MediaType.VIDEO)),
                    null));

    assertThat(outcome).isInstanceOf(PlatformOutcome.Success.class);
    PlatformOutcome.Success success = (PlatformOutcome.Success) outcome;
    assertThat(success.postId()).isEqualTo("vid123");
    assertThat(success.permalink()).isEqualTo("https://youtube.com/shorts/vid123");

    RecordedRequest init = server.takeRequest();
    assertThat(init.getPath()).startsWith("/upload/youtube/v3/videos");
    String body = init.getBody().readUtf8();
    assertThat(body).contains("\"privacyStatus\":\"unlisted\"");
    assertThat(body).contains("My cool short #Shorts");
  }

  @Test
  void rejectsWhenNoVideo() {
    PlatformOutcome outcome = connector().publish(new PublishCommand("hi", List.of(), null));
    assertThat(((PlatformOutcome.Failure) outcome).errorCode()).isEqualTo("NO_MEDIA");
  }
}
