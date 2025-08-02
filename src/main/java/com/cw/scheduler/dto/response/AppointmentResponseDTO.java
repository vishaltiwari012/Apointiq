package com.cw.scheduler.dto.response;

import com.cw.scheduler.entity.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponseDTO {
    private Long id;
    private String individualServiceName;
    private String providerFullName;
    private LocalDateTime appointmentTime;
    private AppointmentStatus status;
}