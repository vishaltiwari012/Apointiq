package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ProviderScheduleRequestDTO;
import com.cw.scheduler.dto.response.ProviderScheduleResponseDTO;
import com.cw.scheduler.entity.OfferedService;
import com.cw.scheduler.entity.ProviderSchedule;
import com.cw.scheduler.entity.ServiceProvider;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.entity.enums.AvailabilityStatus;
import com.cw.scheduler.repository.OfferedServiceRepository;
import com.cw.scheduler.repository.ProviderScheduleRepository;
import com.cw.scheduler.security.AuthenticationFacade;
import com.cw.scheduler.service.interfaces.ProviderScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderScheduleServiceImpl implements ProviderScheduleService {

    private final ProviderScheduleRepository scheduleRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public ApiResponse<ProviderScheduleResponseDTO> createSchedule(ProviderScheduleRequestDTO request) {
        User user = authenticationFacade.getCurrentUser();
        ServiceProvider provider = user.getServiceProvider();

        OfferedService offeredService = offeredServiceRepository.findById(request.getOfferedServiceId())
                .orElseThrow(() -> new RuntimeException("OfferedService not found"));

        if (request.getStatus() == AvailabilityStatus.UNAVAILABLE) {
            if (request.getStartTime() != null || request.getEndTime() != null) {
                throw new IllegalArgumentException("Start and End time must be null when status is NOT_AVAILABLE");
            }
        } else {
            if (request.getStartTime() == null || request.getEndTime() == null) {
                throw new IllegalArgumentException("Start and End time are required when status is AVAILABLE");
            }
            if (!request.getStartTime().isBefore(request.getEndTime())) {
                throw new IllegalArgumentException("Start time must be before end time");
            }
        }


        ProviderSchedule schedule = new ProviderSchedule();
        schedule.setProvider(provider);
        schedule.setService(offeredService);
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setStatus(request.getStatus());

        ProviderSchedule savedSchedule = scheduleRepository.save(schedule);

        ProviderScheduleResponseDTO response = new ProviderScheduleResponseDTO(
                savedSchedule.getId(),
                savedSchedule.getService().getName(),
                savedSchedule.getDayOfWeek(),
                savedSchedule.getStartTime(),
                savedSchedule.getEndTime(),
                savedSchedule.getStatus()
        );

        return ApiResponse.success(response, "Schedule created for service successfully.");

    }

    @Override
    public ApiResponse<List<ProviderScheduleResponseDTO>> getSchedulesForCurrentProvider() {
        User user = authenticationFacade.getCurrentUser();
        ServiceProvider provider = user.getServiceProvider();

        List<ProviderScheduleResponseDTO> response = scheduleRepository.findByProviderId(provider.getId())
                .stream()
                .map(schedule ->
                        new ProviderScheduleResponseDTO(
                                    schedule.getId(),
                                    schedule.getService().getName(),
                                    schedule.getDayOfWeek(),
                                    schedule.getStartTime(),
                                    schedule.getEndTime(),
                                    schedule.getStatus()
                        )).toList();

        return ApiResponse.success(response, "Schedule of current provider");
    }

    @Override
    public ApiResponse<ProviderScheduleResponseDTO> getScheduleForDay(DayOfWeek dayOfWeek) {
        User user = authenticationFacade.getCurrentUser();
        ServiceProvider provider = user.getServiceProvider();

        return scheduleRepository.findByProviderIdAndDayOfWeek(provider.getId(), dayOfWeek)
                .map(schedule -> ApiResponse.success(
                        new ProviderScheduleResponseDTO(
                                schedule.getId(),
                                schedule.getService().getName(),
                                schedule.getDayOfWeek(),
                                schedule.getStartTime(),
                                schedule.getEndTime(),
                                schedule.getStatus()
                        ),
                        "Schedule found for day: " + dayOfWeek))
                .orElse(ApiResponse.error("No schedule found for this day"));
    }

    @Override
    public ApiResponse<List<ProviderScheduleResponseDTO>> getAllSchedulesByDay(DayOfWeek dayOfWeek) {
        List<ProviderScheduleResponseDTO> response = scheduleRepository.findAllByDayOfWeek(dayOfWeek)
                .stream()
                .map(schedule -> new ProviderScheduleResponseDTO(
                        schedule.getId(),
                        schedule.getService().getName(),
                        schedule.getDayOfWeek(),
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        schedule.getStatus()
                )).toList();

        return ApiResponse.success(response, "Schedules found for day: " + dayOfWeek);
    }

    @Override
    public boolean isProviderAvailable(Long providerId, DayOfWeek dayOfWeek, LocalTime time) {
        return scheduleRepository.isProviderAvailableAt(providerId, dayOfWeek, time).isPresent();
    }
}
