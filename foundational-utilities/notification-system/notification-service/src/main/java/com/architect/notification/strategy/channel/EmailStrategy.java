package com.architect.notification.strategy.channel;

import com.architect.notification.model.NotificationEvent;
import com.architect.notification.strategy.NotificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailStrategy implements NotificationStrategy {

    @Override
    public void deliver(NotificationEvent event) {
        log.info("Constructing HTML Email and dispatching to SendGrid API for USER: [{}]", event.getUserId());
        try {
            // Simulating SMTP latency
            Thread.sleep(250); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Email Payload Delivered successfully. Tracking ID: [{}]", event.getTrackingId());
    }

    @Override
    public String getChannelType() {
        return "EMAIL";
    }
}
