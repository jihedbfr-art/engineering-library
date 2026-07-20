package dev.jihed.socialpub.media;

import dev.jihed.socialpub.api.Platform;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * The per-platform media rules from the project spec, expressed as data. Backs both the {@code
 * validate_media} / {@code get_platform_limits} tools and the orchestrator pre-flight. Numbers are
 * the documented API limits, not necessarily the marketing ones.
 *
 * <p>A limit of {@link Long#MAX_VALUE} means "no meaningful cap for our purposes".
 */
public record PlatformConstraints(
    boolean imagesAllowed,
    Set<String> imageMimes,
    long maxImageBytes,
    boolean videosAllowed,
    Set<String> videoMimes,
    long maxVideoBytes,
    long maxVideoDurationMs) {

  private static final long MB = 1024L * 1024L;
  private static final long GB = 1024L * MB;
  private static final long NO_CAP = Long.MAX_VALUE;

  private static final Set<String> JPEG_PNG = Set.of("image/jpeg", "image/png");
  private static final Set<String> JPEG_PNG_WEBP = Set.of("image/jpeg", "image/png", "image/webp");
  private static final Set<String> MP4 = Set.of("video/mp4");
  private static final Set<String> MP4_MOV = Set.of("video/mp4", "video/quicktime");
  private static final Set<String> MP4_WEBM = Set.of("video/mp4", "video/webm");

  private static final Map<Platform, PlatformConstraints> REGISTRY = build();

  public static PlatformConstraints forPlatform(Platform platform) {
    return REGISTRY.getOrDefault(platform, unrestricted());
  }

  public static Map<Platform, PlatformConstraints> all() {
    return Map.copyOf(REGISTRY);
  }

  private static Map<Platform, PlatformConstraints> build() {
    Map<Platform, PlatformConstraints> map = new EnumMap<>(Platform.class);
    // Instagram: feed image up to 8 MB; video (incl. Reels) MP4/MOV up to 100 MB, up to 15 min.
    map.put(
        Platform.INSTAGRAM,
        new PlatformConstraints(true, JPEG_PNG, 8 * MB, true, MP4_MOV, 100 * MB, 15 * 60 * 1000L));
    // Facebook Pages: image up to 10 MB; video effectively large via URL pull.
    map.put(
        Platform.FACEBOOK,
        new PlatformConstraints(true, JPEG_PNG, 10 * MB, true, MP4_MOV, 10 * GB, NO_CAP));
    // TikTok: video only, MP4/WebM up to 4 GB, up to 10 min.
    map.put(
        Platform.TIKTOK,
        new PlatformConstraints(false, Set.of(), 0, true, MP4_WEBM, 4 * GB, 10 * 60 * 1000L));
    // LinkedIn: image up to 36 MB; video up to 500 MB, 3 s - 30 min.
    map.put(
        Platform.LINKEDIN,
        new PlatformConstraints(true, JPEG_PNG, 36 * MB, true, MP4, 500 * MB, 30 * 60 * 1000L));
    // X: image up to 5 MB (JPEG/PNG/WebP); video MP4 up to 512 MB, up to 140 s.
    map.put(
        Platform.X,
        new PlatformConstraints(true, JPEG_PNG_WEBP, 5 * MB, true, MP4, 512 * MB, 140 * 1000L));
    // YouTube Shorts: video only, vertical, up to 3 min.
    map.put(
        Platform.YOUTUBE,
        new PlatformConstraints(false, Set.of(), 0, true, MP4_MOV, 10 * GB, 3 * 60 * 1000L));
    // Mock: anything goes, for demos.
    map.put(Platform.MOCK, unrestricted());
    return map;
  }

  private static PlatformConstraints unrestricted() {
    return new PlatformConstraints(
        true,
        Set.of("image/jpeg", "image/png", "image/webp", "image/gif"),
        NO_CAP,
        true,
        Set.of("video/mp4", "video/quicktime", "video/webm"),
        NO_CAP,
        NO_CAP);
  }
}
