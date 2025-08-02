package com.cw.scheduler.strategy.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationContext {

    private final Map<String, NotificationStrategy> strategies;

    public void send(String strategyKey, String recipient, String message) {
        NotificationStrategy strategy = strategies.get(strategyKey);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found: " + strategyKey);
        }
        strategy.sendNotification(recipient, message);
    }

    public NotificationStrategy getStrategy(String strategyKey) {
        NotificationStrategy strategy = strategies.get(strategyKey);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found: " + strategyKey);
        }
        return strategy;
    }
}

