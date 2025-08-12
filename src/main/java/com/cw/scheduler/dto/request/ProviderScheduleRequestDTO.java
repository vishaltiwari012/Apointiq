package com.cw.scheduler.dto.request;

import com.cw.scheduler.entity.enums.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class ProviderScheduleRequestDTO {
    @NotNull(message = "Offered Service ID is required")
    @Positive(message = "Offered Service ID must be a positive number")
    private Long offeredServiceId;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Availability status is required")
    private AvailabilityStatus status;
}
