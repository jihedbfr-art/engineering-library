package dev.jihed.socialpub.api;

/**
 * A reference to a piece of media.
 *
 * <p>The same record is used at two stages. When a request first arrives only one of {@code
 * sourceUrl} / {@code objectKey} plus a {@link MediaType} is set. After the media module stages it,
 * the remaining fields (mime, size, duration, and a fetchable {@code url}) are filled in and handed
 * to the connectors.
 */
public record MediaRef(
    String url, // fetchable URL a connector can pull from (presigned MinIO or original source)
    String objectKey, // MinIO object key once staged, null before
    MediaType type,
    String mime,
    Long sizeBytes,
    Long durationMs) {

  /** Raw reference to an external URL, before staging. */
  public static MediaRef ofUrl(String sourceUrl, MediaType type) {
    return new MediaRef(sourceUrl, null, type, null, null, null);
  }

  /** Raw reference to an already-staged object. */
  public static MediaRef ofObjectKey(String objectKey, MediaType type) {
    return new MediaRef(null, objectKey, type, null, null, null);
  }
}
