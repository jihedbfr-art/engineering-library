package dev.jihed.socialpub.connector.tiktok;

import dev.jihed.socialpub.api.CredentialProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TikTokProperties.class)
@ConditionalOnProperty(
    prefix = "socialpub.connectors.tiktok",
    name = "enabled",
    havingValue = "true")
public class TikTokConnectorConfig {

  @Bean
  public TikTokConnector tikTokConnector(
      TikTokProperties properties, CredentialProvider credentials) {
    return new TikTokConnector(properties, credentials);
  }
}
