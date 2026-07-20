package dev.jihed.socialpub.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "dev.jihed.socialpub")
@EntityScan("dev.jihed.socialpub.persistence.entity")
@EnableJpaRepositories("dev.jihed.socialpub.persistence.repo")
@EnableScheduling
public class SocialPublisherApplication {

  public static void main(String[] args) {
    SpringApplication.run(SocialPublisherApplication.class, args);
  }
}
