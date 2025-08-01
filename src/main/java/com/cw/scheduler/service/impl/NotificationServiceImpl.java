package com.cw.scheduler.service.impl;

import com.cw.scheduler.service.interfaces.NotificationService;
import com.cw.scheduler.strategy.notification.NotificationStrategy;
import com.cw.scheduler.strategy.notification.TemplatedNotificationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationContext context;

    @Override
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        NotificationStrategy strategy = context.getStrategy("emailNotificationStrategy");
        if (strategy instanceof TemplatedNotificationStrategy) {
            ((TemplatedNotificationStrategy) strategy)
                    .sendTemplatedEmail(to, subject, templateName, variables);
        } else {
            throw new IllegalStateException("Strategy does not support templates");
        }
    }

    @Override
    public void sendOtp(String phone, String otp) {
        context.send("otpNotificationStrategy", phone, otp);
    }
}
