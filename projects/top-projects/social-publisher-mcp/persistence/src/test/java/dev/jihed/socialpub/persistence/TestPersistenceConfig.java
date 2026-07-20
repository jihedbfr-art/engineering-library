package dev.jihed.socialpub.persistence;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/** Minimal bootstrap so the JPA test slice has a configuration to start from. */
@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan("dev.jihed.socialpub.persistence.entity")
@EnableJpaRepositories("dev.jihed.socialpub.persistence.repo")
class TestPersistenceConfig {}
