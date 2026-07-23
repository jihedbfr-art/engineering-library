package dev.jihed.socialpub.app.demo;

import dev.jihed.socialpub.app.mcp.PublishingTools;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A tiny REST surface the demo web page calls. It just forwards to the same {@link PublishingTools}
 * the MCP layer exposes, so what you click in the browser exercises the exact code path Claude
 * would. Only active under the {@code demo} profile.
 */
@RestController
@RequestMapping("/api")
@Profile("demo")
public class DemoController {

  public record PublishBody(String caption, String mediaUrl, List<String> platforms) {}

  public record HashtagBody(String caption, Integer count) {}

  private final PublishingTools tools;

  public DemoController(PublishingTools tools) {
    this.tools = tools;
  }

  @PostMapping("/publish")
  public PublishingTools.PublishPostResult publish(@RequestBody PublishBody body) {
    List<String> media =
        (body.mediaUrl() == null || body.mediaUrl().isBlank())
            ? List.of()
            : List.of(body.mediaUrl());
    return tools.publishPost(body.caption(), media, body.platforms(), null, null);
  }

  @GetMapping("/publications")
  public List<PublishingTools.PublicationSummary> publications() {
    return tools.listPublications(null, null, 20);
  }

  @PostMapping("/hashtags")
  public List<String> hashtags(@RequestBody HashtagBody body) {
    return tools.suggestHashtags(body.caption(), null, body.count());
  }

  @GetMapping("/limits/{platform}")
  public PublishingTools.PlatformLimitsView limits(@PathVariable String platform) {
    return tools.getPlatformLimits(platform);
  }
}
