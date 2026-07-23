package dev.jihed.socialpub.app.demo;

import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.core.port.MediaStager;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * No-op media stager for the local {@code demo} profile — no MinIO, no download. The MOCK connector
 * ignores media anyway, so references are passed straight through.
 */
@Component
@Profile("demo")
public class DemoMediaStager implements MediaStager {

  @Override
  public List<MediaRef> stage(List<MediaRef> rawMedia) {
    return rawMedia;
  }
}
