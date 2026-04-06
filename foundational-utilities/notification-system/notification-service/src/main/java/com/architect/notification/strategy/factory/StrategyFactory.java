package com.architect.notification.strategy.factory;

import com.architect.notification.strategy.NotificationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StrategyFactory {

    private final Map<String, NotificationStrategy> strategies = new HashMap<>();

    @Autowired
    public StrategyFactory(List<NotificationStrategy> strategyList) {
        for (NotificationStrategy strategy : strategyList) {
            strategies.put(strategy.getChannelType().toUpperCase(), strategy);
        }
    }

    public NotificationStrategy getStrategy(String channelType) {
        NotificationStrategy strategy = strategies.get(channelType.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported channel type: " + channelType);
        }
        return strategy;
    }
}
