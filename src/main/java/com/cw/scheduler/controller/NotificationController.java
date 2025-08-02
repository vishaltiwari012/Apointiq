package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.response.NotificationResponseDTO;
import com.cw.scheduler.entity.enums.NotificationType;
import com.cw.scheduler.service.interfaces.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notification APIs")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER', 'SERVICE_PROVIDER')")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponseDTO>>> getMyNotifications() {
        return ResponseEntity.ok(notificationService.getNotificationsForUser());
    }

    @GetMapping("/type")
    public ResponseEntity<ApiResponse<List<NotificationResponseDTO>>> getMyNotificationsByType(
            @RequestParam NotificationType type) {
        return ResponseEntity.ok(notificationService.getNotificationsForUserByType(type));
    }
}
