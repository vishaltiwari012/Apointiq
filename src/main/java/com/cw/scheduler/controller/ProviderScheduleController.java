package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ProviderScheduleRequestDTO;
import com.cw.scheduler.dto.response.ProviderScheduleResponseDTO;
import com.cw.scheduler.ratelimit.RateLimit;
import com.cw.scheduler.service.interfaces.ProviderScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Tag(name = "Schedule APIs")
@RestController
@RequestMapping("/provider-schedule")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SERVICE_PROVIDER')")
public class ProviderScheduleController {

    private final ProviderScheduleService scheduleService;

    @RateLimit(capacity = 5, refillTokens = 1, refillDurationSeconds = 60)
    @Operation(summary = "Create provider schedule", description = "Adds a new schedule for the logged-in service provider.")
    @PostMapping
    public ResponseEntity<ApiResponse<ProviderScheduleResponseDTO>> createSchedule(@RequestBody ProviderScheduleRequestDTO dto) {
        return new ResponseEntity<>(scheduleService.createSchedule(dto), HttpStatus.CREATED);
    }

    @RateLimit(capacity = 10, refillTokens = 2, refillDurationSeconds = 60)
    @Operation(summary = "Get my schedules", description = "Retrieves all schedules for the logged-in service provider.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProviderScheduleResponseDTO>>> getMySchedules() {
        return ResponseEntity.ok(scheduleService.getSchedulesForCurrentProvider());
    }

    @RateLimit(capacity = 10, refillTokens = 2, refillDurationSeconds = 60)
    @Operation(summary = "Get schedule for a day", description = "Retrieves the schedule for the logged-in service provider for a specific day.")
    @GetMapping("/day")
    public ResponseEntity<ApiResponse<ProviderScheduleResponseDTO>> getScheduleForDay(
            @RequestParam String day) {
        DayOfWeek dayOfWeek;
        try {
            dayOfWeek = DayOfWeek.valueOf(day.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid day: " + day));
        }
        return ResponseEntity.ok(scheduleService.getScheduleForDay(dayOfWeek));
    }

    @RateLimit(capacity = 15, refillTokens = 3, refillDurationSeconds = 60)
    @Operation(summary = "Get all schedules by day", description = "Retrieves schedules for all providers for a specific day.")
    @GetMapping("/all/day")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<ProviderScheduleResponseDTO>>> getAllSchedulesByDay(
            @RequestParam String day) {
        try {
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(day.toUpperCase());
            return ResponseEntity.ok(scheduleService.getAllSchedulesByDay(dayOfWeek));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid day: " + day));
        }
    }

    @RateLimit(capacity = 20, refillTokens = 5, refillDurationSeconds = 60)
    @Operation(summary = "Check provider availability", description = "Checks if a provider is available at a specific time on a given day.")
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SERVICE_PROVIDER')")
    public ResponseEntity<Boolean> isProviderAvailable(
            @RequestParam Long providerId,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam String time) {
        LocalTime parsedTime = LocalTime.parse(time);
        return ResponseEntity.ok(scheduleService.isProviderAvailable(providerId, dayOfWeek, parsedTime));
    }
}
