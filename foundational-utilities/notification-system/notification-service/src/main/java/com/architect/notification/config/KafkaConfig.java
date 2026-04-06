package com.architect.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String HIGH_PRIORITY_TOPIC = "notification.high_priority";
    public static final String BULK_TOPIC = "notification.bulk";

    @Bean
    public NewTopic highPriorityTopic() {
        return TopicBuilder.name(HIGH_PRIORITY_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bulkTopic() {
        return TopicBuilder.name(BULK_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
