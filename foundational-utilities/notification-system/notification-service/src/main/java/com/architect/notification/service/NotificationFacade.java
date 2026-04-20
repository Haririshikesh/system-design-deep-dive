package com.architect.notification.service;

import com.architect.notification.config.KafkaConfig;
import com.architect.notification.model.NotificationEvent;
import com.architect.notification.model.NotificationRequest;
import com.architect.notification.model.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationFacade {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationResponse dispatch(NotificationRequest request) {
        String trackingId = "ntf_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        //eg. ntf_a1b2c3d4e5f6
        NotificationEvent event = NotificationEvent.builder()
                .trackingId(trackingId)
                .userId(request.getUserId())
                .channel(request.getChannel())
                .templateId(request.getPayload().getTemplateId())
                .data(request.getPayload().getData())
                .build();

        String topicName = getTopic(request.getPriority());

        kafkaTemplate.send(topicName, trackingId, event);
        log.info("Dispatched event [{}] to Kafka topic [{}]", trackingId, topicName);

        return NotificationResponse.builder()
                .status("QUEUED")
                .trackingId(trackingId)
                .timestamp(Instant.now())
                .build();
    }

    private String getTopic(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) {
            return KafkaConfig.HIGH_PRIORITY_TOPIC;
        }
        return KafkaConfig.BULK_TOPIC;
    }
}
