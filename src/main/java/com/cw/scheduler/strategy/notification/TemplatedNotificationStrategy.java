package com.cw.scheduler.strategy.notification;

import java.util.Map;

public interface TemplatedNotificationStrategy extends NotificationStrategy {
    void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables);
}

