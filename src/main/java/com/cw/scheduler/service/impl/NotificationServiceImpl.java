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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationContext context;
    private final NotificationRepository notificationRepository;
    private final AuthenticationFacade authenticationFacade;
    private final ModelMapper modelMapper;

    @Override
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        log.info("Sending templated email to={}, subject={}, template={}", to, subject, templateName);

        NotificationStrategy strategy = context.getStrategy("emailNotificationStrategy");
        if (strategy instanceof TemplatedNotificationStrategy templatedStrategy) {
            templatedStrategy.sendTemplatedEmail(to, subject, templateName, variables);
        } else {
            log.error("Selected strategy does not support templates");
            throw new IllegalStateException("Strategy does not support templates");
        }
    }

    @Override
    public void sendOtp(String phone, String otp) {
        log.info("Sending OTP to phone={}", phone);
        context.send("otpNotificationStrategy", phone, otp);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "userNotifications", key = "#user.id"),
            @CacheEvict(value = "userNotificationsByType", key = "#user.id + ':' + #type.name()")
    })
    public void saveNotification(User user, String message, NotificationType type) {
        log.info("Saving notification for userId={}, type={}", user.getId(), type);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setSentAt(LocalDateTime.now());
        notification.setType(type);

        notificationRepository.save(notification);
        log.debug("Notification saved for userId={} with message={}", user.getId(), message);
    }

    @Override
    @Cacheable(value = "userNotifications", key = "@authenticationFacade.getCurrentUserId()")
    public ApiResponse<List<NotificationResponseDTO>> getNotificationsForUser() {
        User user = authenticationFacade.getCurrentUser();
        log.info("Fetching all notifications for userId={}", user.getId());

        List<NotificationResponseDTO> response = notificationRepository.findByUserId(user.getId())
                .stream()
                .map(this::toDto)
                .toList();

        log.debug("Found {} notifications for userId={}", response.size(), user.getId());
        return ApiResponse.success(response, "All Notifications of " + user.getName());
    }

    @Override
    @Cacheable(value = "userNotificationsByType", key = "@authenticationFacade.getCurrentUserId() + ':' + #type.name()")
    public ApiResponse<List<NotificationResponseDTO>> getNotificationsForUserByType(NotificationType type) {
        User user = authenticationFacade.getCurrentUser();
        log.info("Fetching notifications for userId={} of type={}", user.getId(), type);

        List<NotificationResponseDTO> response = notificationRepository.findByUserIdAndType(user.getId(), type.name())
                .stream()
                .map(this::toDto)
                .toList();

        log.debug("Found {} notifications for userId={} and type={}", response.size(), user.getId(), type);
        return ApiResponse.success(response, "All Notifications of " + user.getName() + " of type " + type.name());
    }

    private NotificationResponseDTO toDto(Notification notification) {
        return modelMapper.map(notification, NotificationResponseDTO.class);
    }
}
