package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.response.NotificationResponseDTO;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.entity.enums.NotificationType;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    void sendEmail(String to, String subject, String templateName, Map<String, Object> variables);
    void sendOtp(String phone, String otp);
    void saveNotification(User user, String message, NotificationType type);
    ApiResponse<List<NotificationResponseDTO>> getNotificationsForUser();
    ApiResponse<List<NotificationResponseDTO>> getNotificationsForUserByType(NotificationType type);
}
