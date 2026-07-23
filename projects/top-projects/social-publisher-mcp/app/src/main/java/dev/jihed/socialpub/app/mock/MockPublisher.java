package dev.jihed.socialpub.app.mock;

import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.api.PublishCommand;
import dev.jihed.socialpub.api.SocialPublisher;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * A no-network publisher for the {@link Platform#MOCK} target. Only active under the {@code dev}
 * profile so the whole pipeline (DB, staging, fan-out) can be demoed end to end without real API
 * credentials.
 */
@Component
@Profile({"dev", "demo"})
public class MockPublisher implements SocialPublisher {

  @Override
  public Platform platform() {
    return Platform.MOCK;
  }

  @Override
  public PlatformOutcome publish(PublishCommand command) {
    String id = "mock-" + UUID.randomUUID();
    return PlatformOutcome.ok(id, "https://mock.local/posts/" + id);
  }
}
