package dev.jihed.socialpub.core;

import java.util.List;

/** Thrown when a publish request is rejected before any media staging or network call happens. */
public class ValidationException extends RuntimeException {

  private final List<String> violations;

  public ValidationException(List<String> violations) {
    super("Request rejected: " + String.join("; ", violations));
    this.violations = List.copyOf(violations);
  }

  public List<String> violations() {
    return violations;
  }
}
