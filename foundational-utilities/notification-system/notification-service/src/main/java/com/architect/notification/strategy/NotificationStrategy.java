package com.architect.notification.strategy;

import com.architect.notification.model.NotificationEvent;

public interface NotificationStrategy {
    void deliver(NotificationEvent event);
    String getChannelType();
}
