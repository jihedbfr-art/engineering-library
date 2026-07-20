package dev.jihed.socialpub.api;

/** The result of a single platform publish. Either a success carrying the post id, or a failure. */
public sealed interface PlatformOutcome permits PlatformOutcome.Success, PlatformOutcome.Failure {

  boolean success();

  record Success(String postId, String permalink) implements PlatformOutcome {
    @Override
    public boolean success() {
      return true;
    }
  }

  /**
   * A failed publish. {@code retryable} tells the orchestrator whether another attempt is worth it
   * (transient network / rate-limit) or pointless (bad token, rejected media).
   */
  record Failure(String errorCode, String message, boolean retryable) implements PlatformOutcome {
    @Override
    public boolean success() {
      return false;
    }
  }

  static PlatformOutcome ok(String postId, String permalink) {
    return new Success(postId, permalink);
  }

  static PlatformOutcome fail(String errorCode, String message, boolean retryable) {
    return new Failure(errorCode, message, retryable);
  }
}
