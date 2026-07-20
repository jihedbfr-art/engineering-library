package dev.jihed.socialpub.connector.youtube;

import dev.jihed.socialpub.api.CredentialProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(YouTubeProperties.class)
@ConditionalOnProperty(
    prefix = "socialpub.connectors.youtube",
    name = "enabled",
    havingValue = "true")
public class YouTubeConnectorConfig {

  @Bean
  public YouTubeConnector youTubeConnector(
      YouTubeProperties properties, CredentialProvider credentials) {
    return new YouTubeConnector(properties, credentials);
  }
}
