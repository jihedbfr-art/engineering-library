package dev.jihed.socialpub.api;

/**
 * The one interface every platform adapter implements. The MCP layer and the orchestrator only ever
 * see this contract, never the platform SDKs behind it. Adding a network is adding one bean.
 */
public interface SocialPublisher {

  /** Which network this adapter handles. Used as the key in the strategy map. */
  Platform platform();

  /**
   * Publish the command to this platform. Implementations should return a {@link
   * PlatformOutcome.Failure} with {@code retryable=true} for transient problems rather than
   * throwing where they can, but throwing is tolerated — the orchestrator treats an exception as a
   * retryable failure.
   */
  PlatformOutcome publish(PublishCommand command);
}
