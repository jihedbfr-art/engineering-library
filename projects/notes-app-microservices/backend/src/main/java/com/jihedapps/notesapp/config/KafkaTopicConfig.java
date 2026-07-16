package com.jihedapps.notesapp.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${notesapp.kafka.topic-note-events}")
    private String noteEventsTopic;

    @Bean
    public NewTopic noteEventsTopic() {
        return TopicBuilder.name(noteEventsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
