package com.architect.notification.worker;

import com.architect.notification.config.KafkaConfig;
import com.architect.notification.model.NotificationEvent;
import com.architect.notification.strategy.NotificationStrategy;
import com.architect.notification.strategy.factory.StrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWorker {

    private final StrategyFactory strategyFactory;
    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = KafkaConfig.HIGH_PRIORITY_TOPIC, groupId = "notification-group")
    public void processHighPriorityEvent(NotificationEvent event) {
        processEvent(event, "HIGH_PRIORITY_TOPIC");
    }

    @KafkaListener(topics = KafkaConfig.BULK_TOPIC, groupId = "notification-group")
    public void processBulkEvent(NotificationEvent event) {
        processEvent(event, "BULK_TOPIC");
    }

    private void processEvent(NotificationEvent event, String queueName) {
        log.info("[{}] Pulled event [{}] from queue [{}]", 
                 Thread.currentThread().getName(), event.getTrackingId(), queueName);

        String idempotencyKey = "idempotency:ntf:" + event.getTrackingId();
        
        Boolean isDuplicate = redisTemplate.hasKey(idempotencyKey);
        
        if (Boolean.TRUE.equals(isDuplicate)) {
            log.warn("Duplicate message detected for Tracking ID [{}]. Skipping.", event.getTrackingId());
            return;
        }

        try {
            NotificationStrategy strategy = strategyFactory.getStrategy(event.getChannel());
            strategy.deliver(event);
            
            // Set idempotency key upon successful delivery (24 hour TTL)
            redisTemplate.opsForValue().set(idempotencyKey, "PROCESSED", 24, TimeUnit.HOURS);
            
        } catch (Exception e) {
            log.error("Failed to process event [{}]", event.getTrackingId(), e);
            throw new RuntimeException("Requeue message", e); // Will be handled by Spring AMQP retry/DLQ
        }
    }
}
