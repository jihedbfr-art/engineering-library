package dev.jihed.socialpub.media;

import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.MediaType;
import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.core.port.MediaValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Checks each media reference against every target platform's {@link PlatformConstraints}. Fields
 * that are still unknown (null before staging) are simply not checked, so this is safe to call both
 * for the {@code validate_media} dry run and inside the orchestrator once media is staged.
 */
@Component
public class MediaValidatorImpl implements MediaValidator {

  @Override
  public List<String> validate(List<MediaRef> media, Set<Platform> targets) {
    List<String> violations = new ArrayList<>();
    for (Platform platform : targets) {
      PlatformConstraints c = PlatformConstraints.forPlatform(platform);
      for (MediaRef ref : media) {
        violations.addAll(check(platform, c, ref));
      }
    }
    return violations;
  }

  private List<String> check(Platform platform, PlatformConstraints c, MediaRef ref) {
    List<String> out = new ArrayList<>();
    boolean video = ref.type() == MediaType.VIDEO;
    boolean image = ref.type() == MediaType.IMAGE || ref.type() == MediaType.CAROUSEL;

    if (image && !c.imagesAllowed()) {
      out.add(platform + " does not accept images");
      return out;
    }
    if (video && !c.videosAllowed()) {
      out.add(platform + " does not accept video");
      return out;
    }

    if (image) {
      if (ref.mime() != null && !c.imageMimes().contains(ref.mime())) {
        out.add(platform + " rejects image type " + ref.mime());
      }
      if (ref.sizeBytes() != null && ref.sizeBytes() > c.maxImageBytes()) {
        out.add(
            platform
                + " image exceeds "
                + c.maxImageBytes() / (1024 * 1024)
                + " MB ("
                + ref.sizeBytes() / (1024 * 1024)
                + " MB)");
      }
    }

    if (video) {
      if (ref.mime() != null && !c.videoMimes().contains(ref.mime())) {
        out.add(platform + " rejects video type " + ref.mime());
      }
      if (ref.sizeBytes() != null && ref.sizeBytes() > c.maxVideoBytes()) {
        out.add(platform + " video exceeds " + c.maxVideoBytes() / (1024 * 1024) + " MB");
      }
      if (ref.durationMs() != null
          && c.maxVideoDurationMs() != Long.MAX_VALUE
          && ref.durationMs() > c.maxVideoDurationMs()) {
        out.add(
            platform
                + " video too long ("
                + ref.durationMs() / 1000
                + "s > "
                + c.maxVideoDurationMs() / 1000
                + "s)");
      }
    }
    return out;
  }
}
