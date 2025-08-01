package com.cw.scheduler.service.interfaces;

import java.util.Map;

public interface NotificationService {
    void sendEmail(String to, String subject, String templateName, Map<String, Object> variables);
    void sendOtp(String phone, String otp);
}
