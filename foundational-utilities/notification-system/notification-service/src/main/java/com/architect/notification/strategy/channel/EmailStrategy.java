package com.architect.notification.strategy.channel;

import com.architect.notification.model.NotificationEvent;
import com.architect.notification.strategy.NotificationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailStrategy implements NotificationStrategy {

    private final JavaMailSender javaMailSender;

    @Override
    public void deliver(NotificationEvent event) {
        log.info("Constructing Email and dispatching to SMTP server for USER: [{}]", event.getUserId());
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@insightstream.test");
        message.setTo(event.getUserId() + "@example.com"); // Simulate fetching user email
        message.setSubject("New Notification: " + event.getTemplateId());
        
        StringBuilder body = new StringBuilder("Data payload:\n");
        if (event.getData() != null) {
            for (Map.Entry<String, String> entry : event.getData().entrySet()) {
                body.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        } else {
            body.append("No additional content.");
        }
        message.setText(body.toString());

        javaMailSender.send(message);

        log.info("Email Payload Delivered successfully. Tracking ID: [{}]", event.getTrackingId());
    }

    @Override
    public String getChannelType() {
        return "EMAIL";
    }
}
