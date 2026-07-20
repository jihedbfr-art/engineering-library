package dev.jihed.socialpub.app.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jihed.socialpub.core.PublicationService;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers the publishing tools, the publication resource and the cross-post prompt with MCP. */
@Configuration
public class McpToolsConfig {

  private static final String RESOURCE_URI = "publication://{id}";

  @Bean
  public ToolCallbackProvider publishingToolCallbacks(PublishingTools tools) {
    return MethodToolCallbackProvider.builder().toolObjects(tools).build();
  }

  @Bean
  public List<SyncResourceSpecification> publicationResources(
      PublicationService publications, ObjectMapper objectMapper) {
    Resource resource =
        new Resource(
            RESOURCE_URI,
            "publication",
            "The full JSON of a publication (status, per-platform targets and post URLs).",
            "application/json",
            null);

    SyncResourceSpecification spec =
        new SyncResourceSpecification(
            resource,
            (exchange, request) -> {
              UUID id = UUID.fromString(idFromUri(request.uri()));
              String json =
                  publications
                      .status(id)
                      .map(view -> serialize(objectMapper, view))
                      .orElse("{\"error\":\"not found\"}");
              return new ReadResourceResult(
                  List.of(new TextResourceContents(request.uri(), "application/json", json)));
            });
    return List.of(spec);
  }

  @Bean
  public List<SyncPromptSpecification> crossPostPrompt() {
    Prompt prompt =
        new Prompt(
            "cross_post",
            "Adapt one message into platform-native captions before publishing.",
            List.of(
                new PromptArgument("caption", "The core message to adapt", true),
                new PromptArgument("platforms", "Comma-separated target platforms", false)));

    SyncPromptSpecification spec =
        new SyncPromptSpecification(
            prompt,
            (exchange, request) -> {
              Map<String, Object> args = request.arguments();
              String caption = String.valueOf(args.getOrDefault("caption", ""));
              String platforms =
                  String.valueOf(args.getOrDefault("platforms", "LinkedIn, X, Instagram"));
              String text =
                  """
                  Take this message and rewrite it once per platform so each reads natively, \
                  keeping the meaning identical.

                  Message: %s
                  Platforms: %s

                  For each platform give: the adapted caption, then up to 5 relevant hashtags. \
                  Keep X short, let LinkedIn breathe, keep Instagram warm. When ready, call \
                  publish_post with the per-platform overrides."""
                      .formatted(caption, platforms);
              return new GetPromptResult(
                  "cross_post", List.of(new PromptMessage(Role.USER, new TextContent(text))));
            });
    return List.of(spec);
  }

  private static String idFromUri(String uri) {
    int idx = uri.indexOf("://");
    return idx >= 0 ? uri.substring(idx + 3) : uri;
  }

  private static String serialize(ObjectMapper mapper, Object value) {
    try {
      return mapper.writeValueAsString(value);
    } catch (Exception e) {
      return "{\"error\":\"serialization failed\"}";
    }
  }
}
