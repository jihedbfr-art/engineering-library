package dev.jihed.socialpub.media;

import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.MediaType;
import dev.jihed.socialpub.core.port.MediaStager;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Downloads each referenced URL, verifies its type with Tika, probes videos with ffprobe, stores
 * the bytes in MinIO under a content-addressed key (sha-256) and hands back a reference the
 * connectors can pull from via a presigned URL.
 */
@Component
@Profile("!demo")
public class MinioMediaStager implements MediaStager {

  private static final Logger log = LoggerFactory.getLogger(MinioMediaStager.class);
  private static final Map<String, String> EXTENSIONS =
      Map.of(
          "image/jpeg", ".jpg",
          "image/png", ".png",
          "image/webp", ".webp",
          "image/gif", ".gif",
          "video/mp4", ".mp4",
          "video/quicktime", ".mov",
          "video/webm", ".webm");

  private final MinioClient minio;
  private final MediaProperties properties;
  private final Ffprobe ffprobe;
  private final Tika tika = new Tika();
  private final HttpClient http =
      HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(20)).build();
  private volatile boolean bucketReady;

  public MinioMediaStager(MinioClient minio, MediaProperties properties, Ffprobe ffprobe) {
    this.minio = minio;
    this.properties = properties;
    this.ffprobe = ffprobe;
  }

  @Override
  public List<MediaRef> stage(List<MediaRef> rawMedia) {
    ensureBucket();
    List<MediaRef> staged = new ArrayList<>(rawMedia.size());
    for (MediaRef ref : rawMedia) {
      staged.add(stageOne(ref));
    }
    return staged;
  }

  private MediaRef stageOne(MediaRef ref) {
    if (ref.url() == null || ref.url().isBlank()) {
      // Nothing to fetch (already an object key with no source); pass through untouched.
      return ref;
    }
    try {
      byte[] bytes = download(ref.url());
      String mime = tika.detect(bytes);
      String sha256 = sha256(bytes);
      String objectKey = sha256 + EXTENSIONS.getOrDefault(mime, "");

      Long durationMs = null;
      if (isVideo(ref.type(), mime)) {
        durationMs = probeDuration(bytes, objectKey).orElse(null);
      }

      minio.putObject(
          PutObjectArgs.builder().bucket(properties.bucket()).object(objectKey).stream(
                  new ByteArrayInputStream(bytes), bytes.length, -1)
              .contentType(mime)
              .build());

      String url =
          minio.getPresignedObjectUrl(
              GetPresignedObjectUrlArgs.builder()
                  .method(Method.GET)
                  .bucket(properties.bucket())
                  .object(objectKey)
                  .expiry(properties.presignExpirySeconds(), TimeUnit.SECONDS)
                  .build());

      return new MediaRef(url, objectKey, ref.type(), mime, (long) bytes.length, durationMs);
    } catch (Exception e) {
      throw new MediaStagingException("Failed to stage " + ref.url() + ": " + e.getMessage(), e);
    }
  }

  private byte[] download(String url) throws IOException, InterruptedException {
    HttpResponse<byte[]> response =
        http.send(
            HttpRequest.newBuilder(URI.create(url)).GET().build(),
            HttpResponse.BodyHandlers.ofByteArray());
    if (response.statusCode() / 100 != 2) {
      throw new IOException("download returned HTTP " + response.statusCode());
    }
    return response.body();
  }

  private Optional<Long> probeDuration(byte[] bytes, String objectKey) {
    Path tmp = null;
    try {
      tmp = Files.createTempFile("stage-", objectKey.replaceAll("[^A-Za-z0-9.]", ""));
      Files.write(tmp, bytes);
      return ffprobe.probe(tmp).map(Ffprobe.Probe::durationMs);
    } catch (IOException e) {
      log.warn("Could not write temp file for ffprobe: {}", e.getMessage());
      return Optional.empty();
    } finally {
      if (tmp != null) {
        try {
          Files.deleteIfExists(tmp);
        } catch (IOException ignored) {
          // best effort
        }
      }
    }
  }

  private void ensureBucket() {
    if (bucketReady) {
      return;
    }
    try {
      boolean exists =
          minio.bucketExists(BucketExistsArgs.builder().bucket(properties.bucket()).build());
      if (!exists) {
        minio.makeBucket(MakeBucketArgs.builder().bucket(properties.bucket()).build());
      }
      bucketReady = true;
    } catch (Exception e) {
      throw new MediaStagingException("Cannot ensure bucket " + properties.bucket(), e);
    }
  }

  private static boolean isVideo(MediaType type, String mime) {
    return type == MediaType.VIDEO || (mime != null && mime.startsWith("video/"));
  }

  private static String sha256(byte[] bytes) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(bytes);
      StringBuilder sb = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
      }
      return sb.toString();
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
