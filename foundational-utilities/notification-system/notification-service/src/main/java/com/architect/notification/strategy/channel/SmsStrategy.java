package com.architect.notification.strategy.channel;

import com.architect.notification.model.NotificationEvent;
import com.architect.notification.strategy.NotificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsStrategy implements NotificationStrategy {

    @Override
    public void deliver(NotificationEvent event) {
        log.info("Dispatching SMS payload to Twilio API for USER: [{}]", event.getUserId());
        try {
            // Simulating network calls to Twilio
            Thread.sleep(110); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("SMS Delivered successfully. Tracking ID: [{}]", event.getTrackingId());
    }

    @Override
    public String getChannelType() {
        return "SMS";
    }
}
