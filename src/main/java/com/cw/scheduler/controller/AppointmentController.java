package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.AppointmentRequestDTO;
import com.cw.scheduler.dto.response.AppointmentResponseDTO;
import com.cw.scheduler.entity.Appointment;
import com.cw.scheduler.entity.GoogleCalendarToken;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.entity.enums.AppointmentStatus;
import com.cw.scheduler.ratelimit.RateLimit;
import com.cw.scheduler.repository.GoogleCalendarTokenRepository;
import com.cw.scheduler.service.interfaces.AppointmentService;
import com.cw.scheduler.service.interfaces.CalendarIntegrationService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Appointment APIs")
@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER', 'SERVICE_PROVIDER')")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final GoogleAuthorizationCodeFlow googleFlow;
    private final GoogleCalendarTokenRepository tokenRepository;
    private final CalendarIntegrationService calendarIntegrationService;

    @RateLimit(capacity = 10, refillTokens = 3, refillDurationSeconds = 60)
    @Operation(summary = "Book appointment", description = "Books a new appointment for the logged-in customer. Handles Google Calendar integration consent flow.")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> bookAppointment(@RequestBody @Valid AppointmentRequestDTO dto, HttpServletResponse response) throws IOException {
        Appointment appointment = appointmentService.bookAppointmentAndReturnEntity(dto);
        User user = appointment.getUser();

        // If token exists, skip consent screen
        Optional<GoogleCalendarToken> existingToken = tokenRepository.findById(user.getId());
        if (existingToken.isPresent()) {
            try {
                Credential credential = calendarIntegrationService.getCredentialForUser(user, googleFlow);
                String eventId = calendarIntegrationService.addEventToCalendar(appointment, user, credential);
                appointment.setCalendarEventId(eventId);
                appointmentService.save(appointment);
                return ResponseEntity.ok(Map.of("redirect", "http://localhost:8085/api/v1/appointments/my"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Else ask for consent
        String authUrl = googleFlow.newAuthorizationUrl()
                .setRedirectUri("http://localhost:8085/api/v1/google/calendar/callback")
                .setState(String.valueOf(appointment.getId()))
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        return ResponseEntity.ok(Map.of("redirect", authUrl));
    }

    @RateLimit(capacity = 15, refillTokens = 5, refillDurationSeconds = 60)
    @Operation(summary = "Get my appointments", description = "Fetches all appointments booked by the logged-in customer.")
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDTO>>> getMyAppointments() {
        return ResponseEntity.ok(appointmentService.getAppointmentsForCurrentUser());
    }

    @RateLimit(capacity = 10, refillTokens = 3, refillDurationSeconds = 60)
    @Operation(summary = "Cancel appointment", description = "Cancels an appointment by ID for the logged-in customer.")
    @DeleteMapping("/{appointmentId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<String>> cancelAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(appointmentId));
    }

    @RateLimit(capacity = 15, refillTokens = 5, refillDurationSeconds = 60)
    @Operation(summary = "Get provider appointments", description = "Fetches all appointments for the logged-in service provider.")
    @GetMapping("/provider")
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDTO>>> getProviderAppointments() {
        return ResponseEntity.ok(appointmentService.getProviderAppointments());
    }

    @RateLimit(capacity = 15, refillTokens = 5, refillDurationSeconds = 60)
    @Operation(summary = "Get upcoming appointments", description = "Fetches upcoming appointments for the logged-in service provider.")
    @GetMapping("/provider/upcoming")
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDTO>>> getUpcomingAppointments() {
        return ResponseEntity.ok(appointmentService.getUpcomingAppointments());
    }

    @RateLimit(capacity = 10, refillTokens = 3, refillDurationSeconds = 60)
    @Operation(summary = "Update appointment status", description = "Updates the status of an appointment by ID for the logged-in service provider.")
    @PutMapping("/{appointmentId}/status")
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> updateStatus(
            @PathVariable Long appointmentId,
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(appointmentId, status));
    }

    @RateLimit(capacity = 15, refillTokens = 5, refillDurationSeconds = 60)
    @Operation(summary = "Get provider appointments by date", description = "Fetches appointments for the logged-in service provider filtered by date.")
    @GetMapping("/provider/date")
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDTO>>> getAppointmentsForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForDate(date));
    }
}


//    @PostMapping
//    @PreAuthorize("hasRole('CUSTOMER')")
//    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> bookAppointment(
//            @RequestBody @Valid AppointmentRequestDTO dto) {
//        return new ResponseEntity<>(appointmentService.bookAppointment(dto), HttpStatus.CREATED);
//    }