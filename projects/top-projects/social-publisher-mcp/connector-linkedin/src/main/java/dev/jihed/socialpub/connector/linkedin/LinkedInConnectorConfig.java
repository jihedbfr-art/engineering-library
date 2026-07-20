package dev.jihed.socialpub.connector.linkedin;

import dev.jihed.socialpub.api.CredentialProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LinkedInProperties.class)
@ConditionalOnProperty(
    prefix = "socialpub.connectors.linkedin",
    name = "enabled",
    havingValue = "true")
public class LinkedInConnectorConfig {

  @Bean
  public LinkedInConnector linkedInConnector(
      LinkedInProperties properties, CredentialProvider credentials) {
    return new LinkedInConnector(properties, credentials);
  }
}
