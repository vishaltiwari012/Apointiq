package com.cw.scheduler.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequestDTO {
    @NotNull
    private Long individualServiceId;

    @NotNull
    private LocalDateTime appointmentTime;
}