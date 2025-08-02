package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.response.NotificationResponseDTO;
import com.cw.scheduler.entity.Notification;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.entity.enums.NotificationType;
import com.cw.scheduler.repository.NotificationRepository;
import com.cw.scheduler.security.AuthenticationFacade;
import com.cw.scheduler.service.interfaces.NotificationService;
import com.cw.scheduler.strategy.notification.NotificationContext;
import com.cw.scheduler.strategy.notification.NotificationStrategy;
import com.cw.scheduler.strategy.notification.TemplatedNotificationStrategy;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationContext context;
    private final NotificationRepository notificationRepository;
    private final AuthenticationFacade authenticationFacade;
    private final ModelMapper modelMapper;

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

    @Override
    public void saveNotification(User user, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setSentAt(LocalDateTime.now());
        notification.setType(type);

        notificationRepository.save(notification);
    }

    @Override
    public ApiResponse<List<NotificationResponseDTO>> getNotificationsForUser() {
        User user = authenticationFacade.getCurrentUser();
        List<NotificationResponseDTO> response = notificationRepository.findByUserId(user.getId())
                .stream().map(notification -> modelMapper.map(notification, NotificationResponseDTO.class))
                .toList();

        return ApiResponse.success(response, "All Notifications of " + user.getName());
    }

    @Override
    public ApiResponse<List<NotificationResponseDTO>> getNotificationsForUserByType(NotificationType type) {
        User user = authenticationFacade.getCurrentUser();
        List<NotificationResponseDTO> response = notificationRepository.findByUserIdAndType(user.getId(), type.name())
                .stream().map(notification -> modelMapper.map(notification, NotificationResponseDTO.class))
                .toList();

        return ApiResponse.success(response, "All Notifications of " + user.getName() + " of type " + type.name());
    }
}
