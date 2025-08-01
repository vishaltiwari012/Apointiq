package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ProviderScheduleRequestDTO;
import com.cw.scheduler.dto.response.ProviderScheduleResponseDTO;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface ProviderScheduleService {
    ApiResponse<ProviderScheduleResponseDTO> createSchedule(ProviderScheduleRequestDTO request);
    ApiResponse<List<ProviderScheduleResponseDTO>> getSchedulesForCurrentProvider();
    ApiResponse<ProviderScheduleResponseDTO> getScheduleForDay(DayOfWeek dayOfWeek);
    ApiResponse<List<ProviderScheduleResponseDTO>> getAllSchedulesByDay(DayOfWeek dayOfWeek);
    boolean isProviderAvailable(Long providerId, DayOfWeek dayOfWeek, LocalTime time);
}
