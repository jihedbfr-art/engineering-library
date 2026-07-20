package dev.jihed.socialpub.connector.facebook;

import dev.jihed.socialpub.api.CredentialProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FacebookProperties.class)
@ConditionalOnProperty(
    prefix = "socialpub.connectors.facebook",
    name = "enabled",
    havingValue = "true")
public class FacebookConnectorConfig {

  @Bean
  public FacebookConnector facebookConnector(
      FacebookProperties properties, CredentialProvider credentials) {
    return new FacebookConnector(properties, credentials);
  }
}
