package dev.jihed.socialpub.core.port;

import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.Platform;
import java.util.List;
import java.util.Set;

/**
 * Checks media against each target platform's size/duration/format rules. Returns a list of
 * human-readable violations; an empty list means the media is acceptable everywhere. Implemented by
 * the {@code media} module (backed by the PlatformConstraints registry).
 */
public interface MediaValidator {

  List<String> validate(List<MediaRef> media, Set<Platform> targets);
}
