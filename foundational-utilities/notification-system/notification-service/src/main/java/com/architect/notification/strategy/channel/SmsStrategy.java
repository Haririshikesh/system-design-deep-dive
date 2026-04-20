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
        log.info("Initiating SMS dispatch process for USER: [{}]", event.getUserId());
        
        // 1. Validation (Simulated)
        if (event.getUserId() == null || event.getUserId().isBlank()) {
            log.error("SMS Dispatch aborted: Invalid User ID for Tracking ID [{}]", event.getTrackingId());
            return;
        }

        // 2. Resolve Contact Info (Simulated lookup)
        String phoneNumber = "+15550109981"; // Mock data
        log.info("Resolved phone number [{}] for USER [{}]", phoneNumber, event.getUserId());

        // 3. Dispatch to Provider (Simulated Twilio/Amazon SNS call)
        try {
            log.info("Calling 3rd-party SMS Provider (Twilio) for Tracking ID: [{}]", event.getTrackingId());
            // Real SDK calls would go here: smsProvider.send(phoneNumber, event.getData().get("message"));
            Thread.sleep(150); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("SMS Dispatch interrupted", e);
        }

        log.info("SMS successfully handshaked with Provider. Tracking ID: [{}]", event.getTrackingId());
    }

    @Override
    public String getChannelType() {
        return "SMS";
    }
}
