package com.cw.scheduler.dto.response;

import com.cw.scheduler.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDTO {
    private Long id;
    private String message;
    private LocalDateTime sentAt;
    private NotificationType type;
}