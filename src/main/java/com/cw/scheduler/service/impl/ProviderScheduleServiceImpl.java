package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ProviderScheduleRequestDTO;
import com.cw.scheduler.dto.response.ProviderScheduleResponseDTO;
import com.cw.scheduler.entity.OfferedService;
import com.cw.scheduler.entity.ProviderSchedule;
import com.cw.scheduler.entity.ServiceProvider;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.entity.enums.AvailabilityStatus;
import com.cw.scheduler.exception.BadRequestException;
import com.cw.scheduler.exception.ResourceNotFoundException;
import com.cw.scheduler.repository.OfferedServiceRepository;
import com.cw.scheduler.repository.ProviderScheduleRepository;
import com.cw.scheduler.security.AuthenticationFacade;
import com.cw.scheduler.service.interfaces.ProviderScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderScheduleServiceImpl implements ProviderScheduleService {

    private final ProviderScheduleRepository scheduleRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "providerSchedules", key = "@authenticationFacade.getCurrentUser().serviceProvider.id"),
            @CacheEvict(value = "allSchedulesByDay", allEntries = true)
    })
    public ApiResponse<ProviderScheduleResponseDTO> createSchedule(ProviderScheduleRequestDTO request) {
        User user = authenticationFacade.getCurrentUser();
        ServiceProvider provider = user.getServiceProvider();

        log.info("Creating schedule for providerId={}, serviceId={}, day={}",
                provider.getId(), request.getOfferedServiceId(), request.getDayOfWeek());

        OfferedService offeredService = offeredServiceRepository.findById(request.getOfferedServiceId())
                .orElseThrow(() -> {
                    log.warn("OfferedService not found for id={}", request.getOfferedServiceId());
                    return new ResourceNotFoundException("OfferedService not found");
                });

        validateScheduleRequest(request);

        ProviderSchedule schedule = new ProviderSchedule();
        schedule.setProvider(provider);
        schedule.setService(offeredService);
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setStatus(request.getStatus());

        ProviderSchedule savedSchedule = scheduleRepository.save(schedule);
        log.debug("Schedule saved with id={} for providerId={}", savedSchedule.getId(), provider.getId());

        return ApiResponse.success(toDto(savedSchedule), "Schedule created for service successfully.");
    }

    @Override
    @Cacheable(value = "providerSchedules", key = "@authenticationFacade.getCurrentUser().serviceProvider.id")
    public ApiResponse<List<ProviderScheduleResponseDTO>> getSchedulesForCurrentProvider() {
        ServiceProvider provider = authenticationFacade.getCurrentUser().getServiceProvider();
        log.info("Fetching schedules for providerId={}", provider.getId());

        List<ProviderScheduleResponseDTO> response = scheduleRepository.findByProviderId(provider.getId())
                .stream()
                .map(this::toDto)
                .toList();

        log.debug("Found {} schedules for providerId={}", response.size(), provider.getId());
        return ApiResponse.success(response, "Schedule of current provider");
    }

    @Override
    @Cacheable(
            value = "providerScheduleForDay",
            key = "@authenticationFacade.getCurrentUser().serviceProvider.id + '-' + #dayOfWeek"
    )
    public ApiResponse<ProviderScheduleResponseDTO> getScheduleForDay(DayOfWeek dayOfWeek) {
        ServiceProvider provider = authenticationFacade.getCurrentUser().getServiceProvider();
        log.info("Fetching schedule for providerId={} on {}", provider.getId(), dayOfWeek);

        return scheduleRepository.findByProviderIdAndDayOfWeek(provider.getId(), dayOfWeek)
                .map(schedule -> ApiResponse.success(toDto(schedule), "Schedule found for day: " + dayOfWeek))
                .orElse(ApiResponse.error("No schedule found for this day"));
    }

    @Override
    @Cacheable(value = "allSchedulesByDay", key = "#dayOfWeek")
    public ApiResponse<List<ProviderScheduleResponseDTO>> getAllSchedulesByDay(DayOfWeek dayOfWeek) {
        log.info("Fetching all schedules for day={}", dayOfWeek);

        List<ProviderScheduleResponseDTO> response = scheduleRepository.findAllByDayOfWeek(dayOfWeek)
                .stream()
                .map(this::toDto)
                .toList();

        log.debug("Found {} schedules for day={}", response.size(), dayOfWeek);
        return ApiResponse.success(response, "Schedules found for day: " + dayOfWeek);
    }

    @Override
    public boolean isProviderAvailable(Long providerId, DayOfWeek dayOfWeek, LocalTime time) {
        boolean available = scheduleRepository.isProviderAvailableAt(providerId, dayOfWeek, time).isPresent();
        log.debug("Provider availability check: providerId={}, day={}, time={}, available={}",
                providerId, dayOfWeek, time, available);
        return available;
    }


     /**
         Utility Methods
     */
    private void validateScheduleRequest(ProviderScheduleRequestDTO request) {
        if (request.getStatus() == AvailabilityStatus.UNAVAILABLE) {
            if (request.getStartTime() != null || request.getEndTime() != null) {
                throw new BadRequestException("Start and End time must be null when status is UNAVAILABLE");
            }
        } else {
            if (request.getStartTime() == null || request.getEndTime() == null) {
                throw new BadRequestException("Start and End time are required when status is AVAILABLE");
            }
            if (!request.getStartTime().isBefore(request.getEndTime())) {
                throw new BadRequestException("Start time must be before end time");
            }
        }
    }


    private ProviderScheduleResponseDTO toDto(ProviderSchedule schedule) {
        return new ProviderScheduleResponseDTO(
                schedule.getId(),
                schedule.getService().getName(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getStatus()
        );
    }
}
