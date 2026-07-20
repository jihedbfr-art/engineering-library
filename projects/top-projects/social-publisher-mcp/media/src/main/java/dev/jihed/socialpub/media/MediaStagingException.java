package dev.jihed.socialpub.media;

/** Raised when media cannot be fetched, probed or stored. */
public class MediaStagingException extends RuntimeException {

  public MediaStagingException(String message, Throwable cause) {
    super(message, cause);
  }
}
