package dev.jihed.socialpub.app.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jihed.socialpub.app.credentials.CredentialService;
import dev.jihed.socialpub.core.PublicationService;
import dev.jihed.socialpub.core.port.MediaValidator;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;

class McpToolsConfigTest {

  private final McpToolsConfig config = new McpToolsConfig();

  private PublishingTools tools() {
    return new PublishingTools(
        mock(PublicationService.class), mock(CredentialService.class), mock(MediaValidator.class));
  }

  @Test
  void registersTheEightNamedTools() {
    ToolCallbackProvider provider = config.publishingToolCallbacks(tools());
    var names =
        Arrays.stream(provider.getToolCallbacks())
            .map(c -> c.getToolDefinition().name())
            .collect(Collectors.toSet());
    assertThat(names)
        .containsExactlyInAnyOrder(
            "publish_post",
            "list_connected_accounts",
            "get_publication_status",
            "list_publications",
            "cancel_scheduled_post",
            "validate_media",
            "suggest_hashtags",
            "get_platform_limits");
  }

  @Test
  void exposesThePublicationResource() {
    var specs = config.publicationResources(mock(PublicationService.class), new ObjectMapper());
    assertThat(specs).hasSize(1);
    assertThat(specs.get(0).resource().uri()).isEqualTo("publication://{id}");
    assertThat(specs.get(0).resource().mimeType()).isEqualTo("application/json");
  }

  @Test
  void exposesTheCrossPostPrompt() {
    var specs = config.crossPostPrompt();
    assertThat(specs).hasSize(1);
    assertThat(specs.get(0).prompt().name()).isEqualTo("cross_post");
    assertThat(specs.get(0).prompt().arguments()).extracting("name").contains("caption");
  }
}
