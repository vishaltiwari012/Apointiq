package com.cw.scheduler.dto.response;

import com.cw.scheduler.entity.enums.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderScheduleResponseDTO {
    private Long id;
    private String offeredServiceName;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private AvailabilityStatus status;
}

