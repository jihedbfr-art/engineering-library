package dev.jihed.socialpub.connector.instagram;

import dev.jihed.socialpub.api.CredentialProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InstagramProperties.class)
@ConditionalOnProperty(
    prefix = "socialpub.connectors.instagram",
    name = "enabled",
    havingValue = "true")
public class InstagramConnectorConfig {

  @Bean
  public InstagramConnector instagramConnector(
      InstagramProperties properties, CredentialProvider credentials) {
    return new InstagramConnector(properties, credentials);
  }
}
