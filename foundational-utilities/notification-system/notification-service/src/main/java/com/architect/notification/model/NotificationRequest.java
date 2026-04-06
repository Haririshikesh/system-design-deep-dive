package com.architect.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String userId;
    private String channel; // "SMS", "EMAIL", "PUSH"
    private String priority; // "HIGH", "BULK"
    private Payload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private String templateId;
        private Map<String, String> data;
    }
}
