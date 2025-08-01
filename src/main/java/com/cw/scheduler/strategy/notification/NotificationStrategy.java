package com.cw.scheduler.strategy.notification;

public interface NotificationStrategy {
    void sendNotification(String recipient, String message);
}
