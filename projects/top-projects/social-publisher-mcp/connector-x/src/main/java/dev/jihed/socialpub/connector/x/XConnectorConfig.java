package dev.jihed.socialpub.connector.x;

import dev.jihed.socialpub.api.CredentialProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(XProperties.class)
@ConditionalOnProperty(prefix = "socialpub.connectors.x", name = "enabled", havingValue = "true")
public class XConnectorConfig {

  @Bean
  public XConnector xConnector(XProperties properties, CredentialProvider credentials) {
    return new XConnector(properties, credentials);
  }
}
