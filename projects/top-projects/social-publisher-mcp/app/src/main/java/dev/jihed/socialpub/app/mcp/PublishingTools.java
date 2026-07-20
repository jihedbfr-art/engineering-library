package dev.jihed.socialpub.app.mcp;

import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.MediaType;
import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.api.PlatformOverrides;
import dev.jihed.socialpub.app.credentials.CredentialService;
import dev.jihed.socialpub.core.PublicationService;
import dev.jihed.socialpub.core.PublicationStatus;
import dev.jihed.socialpub.core.PublicationView;
import dev.jihed.socialpub.core.PublishRequest;
import dev.jihed.socialpub.core.PublishResult;
import dev.jihed.socialpub.core.ValidationException;
import dev.jihed.socialpub.core.port.MediaValidator;
import dev.jihed.socialpub.media.PlatformConstraints;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** The MCP toolset an assistant uses to publish and inspect posts. */
@Component
public class PublishingTools {

  private static final Logger log = LoggerFactory.getLogger(PublishingTools.class);

  private final PublicationService publications;
  private final CredentialService credentials;
  private final MediaValidator mediaValidator;
  private final HttpClient http =
      HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(15)).build();

  public PublishingTools(
      PublicationService publications,
      CredentialService credentials,
      MediaValidator mediaValidator) {
    this.publications = publications;
    this.credentials = credentials;
    this.mediaValidator = mediaValidator;
  }

  // ----- tool input/output shapes -----

  public record PerPlatformOverride(
      String platform, String caption, List<String> hashtags, String title) {}

  public record PlatformOutcomeView(
      String platform,
      boolean success,
      String postId,
      String permalink,
      String errorCode,
      String message) {}

  public record PublishPostResult(
      String publicationId, String status, List<PlatformOutcomeView> outcomes, String note) {}

  public record ConnectedAccountView(String platform, String tokenExpiresAt, boolean valid) {}

  public record TargetView(
      String platform, String status, String postId, String permalink, String error) {}

  public record PublicationStatusView(
      String publicationId,
      String status,
      String caption,
      String scheduledAt,
      List<TargetView> targets) {}

  public record PublicationSummary(
      String publicationId, String status, String caption, String scheduledAt) {}

  public record CancelResult(boolean cancelled, String message) {}

  public record ValidateMediaResult(boolean ok, List<String> violations) {}

  public record PlatformLimitsView(
      String platform,
      boolean imagesAllowed,
      Set<String> imageFormats,
      long maxImageMB,
      boolean videosAllowed,
      Set<String> videoFormats,
      long maxVideoMB,
      long maxVideoSeconds) {}

  // ----- tools -----

  @Tool(
      name = "publish_post",
      description =
          "Publish a caption and optional media to one or more social networks. Platforms:"
              + " INSTAGRAM, FACEBOOK, TIKTOK, LINKEDIN, X, YOUTUBE. Omit scheduledAt to publish"
              + " immediately, or pass an ISO-8601 instant to schedule. Returns a per-platform"
              + " outcome with the post URL on success.")
  public PublishPostResult publishPost(
      @ToolParam(description = "The caption / text body of the post") String caption,
      @ToolParam(description = "Public URLs of images or videos to attach", required = false)
          List<String> mediaUrls,
      @ToolParam(description = "Target platform names") List<String> platforms,
      @ToolParam(
              description =
                  "ISO-8601 instant (e.g. 2026-08-01T09:00:00Z) to schedule; omit for now",
              required = false)
          String scheduledAt,
      @ToolParam(
              description = "Optional per-platform caption/hashtag/title overrides",
              required = false)
          List<PerPlatformOverride> overrides) {

    Set<Platform> targets = parsePlatforms(platforms);
    List<MediaRef> media = new ArrayList<>();
    if (mediaUrls != null) {
      for (String url : mediaUrls) {
        media.add(MediaRef.ofUrl(url, inferType(url)));
      }
    }
    Instant when =
        (scheduledAt == null || scheduledAt.isBlank()) ? null : Instant.parse(scheduledAt);
    PublishRequest request =
        new PublishRequest(caption, media, targets, when, parseOverrides(overrides));

    try {
      PublishResult result = publications.publish(request);
      List<PlatformOutcomeView> views = new ArrayList<>();
      result.outcomes().forEach((platform, outcome) -> views.add(toOutcomeView(platform, outcome)));
      String note =
          views.isEmpty() && result.overallStatus() == PublicationStatus.SCHEDULED
              ? "Scheduled; it will be published at the requested time."
              : null;
      return new PublishPostResult(
          result.publicationId().toString(), result.overallStatus().name(), views, note);
    } catch (ValidationException e) {
      return new PublishPostResult(null, "REJECTED", List.of(), String.join("; ", e.violations()));
    }
  }

  @Tool(
      name = "list_connected_accounts",
      description =
          "List the platforms that currently have stored credentials, with token expiry and whether"
              + " the token is still valid.")
  public List<ConnectedAccountView> listConnectedAccounts() {
    return credentials.listConnected().stream()
        .map(
            a ->
                new ConnectedAccountView(
                    a.platform().name(),
                    a.expiresAt() == null ? null : a.expiresAt().toString(),
                    a.valid()))
        .toList();
  }

  @Tool(
      name = "get_publication_status",
      description = "Get the status, per-platform outcomes and post URLs for a publication id.")
  public PublicationStatusView getPublicationStatus(
      @ToolParam(description = "The publication id returned by publish_post")
          String publicationId) {
    PublicationView view =
        publications
            .status(UUID.fromString(publicationId))
            .orElseThrow(() -> new IllegalArgumentException("No publication " + publicationId));
    return toStatusView(view);
  }

  @Tool(
      name = "list_publications",
      description =
          "List recent publications, optionally filtered by status and/or platform. Default limit"
              + " is 20.")
  public List<PublicationSummary> listPublications(
      @ToolParam(
              description = "Filter by status (e.g. PUBLISHED, SCHEDULED, FAILED)",
              required = false)
          String status,
      @ToolParam(description = "Filter by platform", required = false) String platform,
      @ToolParam(description = "Max rows to return", required = false) Integer limit) {
    PublicationStatus statusFilter =
        (status == null || status.isBlank())
            ? null
            : PublicationStatus.valueOf(status.toUpperCase(Locale.ROOT));
    Platform platformFilter =
        (platform == null || platform.isBlank()) ? null : parsePlatform(platform);
    int max = (limit == null || limit <= 0) ? 20 : limit;
    return publications.history(statusFilter, platformFilter, max).stream()
        .map(
            v ->
                new PublicationSummary(
                    v.id().toString(),
                    v.status().name(),
                    v.caption(),
                    v.scheduledAt() == null ? null : v.scheduledAt().toString()))
        .toList();
  }

  @Tool(
      name = "cancel_scheduled_post",
      description = "Cancel a scheduled publication. Only works while it is still SCHEDULED.")
  public CancelResult cancelScheduledPost(
      @ToolParam(description = "The publication id to cancel") String publicationId) {
    boolean cancelled = publications.cancelScheduled(UUID.fromString(publicationId));
    return new CancelResult(
        cancelled, cancelled ? "Cancelled." : "Not cancelled — it is not in a SCHEDULED state.");
  }

  @Tool(
      name = "validate_media",
      description =
          "Dry-run check of a media URL against each platform's format and size rules, without"
              + " publishing. Duration is only checked at publish time.")
  public ValidateMediaResult validateMedia(
      @ToolParam(description = "Public URL of the image or video") String mediaUrl,
      @ToolParam(description = "Platforms to check against") List<String> platforms) {
    Set<Platform> targets = parsePlatforms(platforms);
    MediaRef inspected = inspect(mediaUrl);
    List<String> violations = mediaValidator.validate(List.of(inspected), targets);
    return new ValidateMediaResult(violations.isEmpty(), violations);
  }

  @Tool(
      name = "suggest_hashtags",
      description =
          "Suggest hashtags for a caption using simple keyword extraction (tokenize, drop stopwords,"
              + " rank by frequency). Deterministic, no external model call.")
  public List<String> suggestHashtags(
      @ToolParam(description = "The caption to derive hashtags from") String caption,
      @ToolParam(
              description = "Target platform (currently only affects count conventions)",
              required = false)
          String platform,
      @ToolParam(description = "How many hashtags to return (default 10)", required = false)
          Integer count) {
    int n = (count == null || count <= 0) ? 10 : count;
    return Hashtags.suggest(caption, n);
  }

  @Tool(
      name = "get_platform_limits",
      description =
          "Return the media constraints (formats, max size, max duration) for a platform.")
  public PlatformLimitsView getPlatformLimits(
      @ToolParam(description = "Platform name") String platform) {
    Platform p = parsePlatform(platform);
    PlatformConstraints c = PlatformConstraints.forPlatform(p);
    return new PlatformLimitsView(
        p.name(),
        c.imagesAllowed(),
        c.imageMimes(),
        toMB(c.maxImageBytes()),
        c.videosAllowed(),
        c.videoMimes(),
        toMB(c.maxVideoBytes()),
        c.maxVideoDurationMs() == Long.MAX_VALUE ? -1 : c.maxVideoDurationMs() / 1000);
  }

  // ----- helpers -----

  private PlatformOutcomeView toOutcomeView(Platform platform, PlatformOutcome outcome) {
    if (outcome instanceof PlatformOutcome.Success s) {
      return new PlatformOutcomeView(platform.name(), true, s.postId(), s.permalink(), null, null);
    }
    PlatformOutcome.Failure f = (PlatformOutcome.Failure) outcome;
    return new PlatformOutcomeView(platform.name(), false, null, null, f.errorCode(), f.message());
  }

  private PublicationStatusView toStatusView(PublicationView view) {
    List<TargetView> targets =
        view.targets().stream()
            .map(
                t ->
                    new TargetView(
                        t.platform().name(),
                        t.status().name(),
                        t.externalPostId(),
                        t.permalink(),
                        t.errorCode() == null ? null : t.errorCode() + ": " + t.errorMessage()))
            .toList();
    return new PublicationStatusView(
        view.id().toString(),
        view.status().name(),
        view.caption(),
        view.scheduledAt() == null ? null : view.scheduledAt().toString(),
        targets);
  }

  private Map<Platform, PlatformOverrides> parseOverrides(List<PerPlatformOverride> overrides) {
    if (overrides == null || overrides.isEmpty()) {
      return Map.of();
    }
    Map<Platform, PlatformOverrides> map = new EnumMap<>(Platform.class);
    for (PerPlatformOverride o : overrides) {
      map.put(
          parsePlatform(o.platform()), new PlatformOverrides(o.caption(), o.hashtags(), o.title()));
    }
    return map;
  }

  private Set<Platform> parsePlatforms(List<String> names) {
    if (names == null || names.isEmpty()) {
      return Set.of();
    }
    Set<Platform> set = java.util.EnumSet.noneOf(Platform.class);
    for (String name : names) {
      set.add(parsePlatform(name));
    }
    return set;
  }

  private Platform parsePlatform(String name) {
    try {
      return Platform.valueOf(name.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Unknown platform '"
              + name
              + "'. Valid: INSTAGRAM, FACEBOOK, TIKTOK, LINKEDIN, X, YOUTUBE");
    }
  }

  private static MediaType inferType(String url) {
    String lower = url.toLowerCase(Locale.ROOT);
    if (lower.matches(".*\\.(mp4|mov|webm|m4v)(\\?.*)?$")) {
      return MediaType.VIDEO;
    }
    return MediaType.IMAGE;
  }

  private MediaRef inspect(String url) {
    MediaType type = inferType(url);
    try {
      HttpResponse<Void> response =
          http.send(
              HttpRequest.newBuilder(URI.create(url))
                  .method("HEAD", HttpRequest.BodyPublishers.noBody())
                  .build(),
              HttpResponse.BodyHandlers.discarding());
      String mime =
          response
              .headers()
              .firstValue("content-type")
              .map(s -> s.split(";")[0].trim())
              .orElse(null);
      Long size =
          response.headers().firstValueAsLong("content-length").isPresent()
              ? response.headers().firstValueAsLong("content-length").getAsLong()
              : null;
      return new MediaRef(url, null, type, mime, size, null);
    } catch (Exception e) {
      log.warn("HEAD failed for {}: {}", url, e.getMessage());
      return MediaRef.ofUrl(url, type);
    }
  }

  private static long toMB(long bytes) {
    if (bytes == Long.MAX_VALUE) {
      return -1;
    }
    return bytes / (1024 * 1024);
  }
}
