package dev.jihed.socialpub.core.port;

import dev.jihed.socialpub.api.MediaRef;
import java.util.List;

/**
 * Downloads raw media, stores it in the object store and returns enriched references (with a
 * fetchable URL, mime, size and duration filled in). Implemented by the {@code media} module.
 */
public interface MediaStager {

  List<MediaRef> stage(List<MediaRef> rawMedia);
}
