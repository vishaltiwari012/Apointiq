package com.cw.scheduler.controller;

import com.cw.scheduler.entity.Appointment;
import com.cw.scheduler.entity.GoogleCalendarToken;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.exception.ResourceNotFoundException;
import com.cw.scheduler.repository.AppointmentRepository;
import com.cw.scheduler.repository.GoogleCalendarTokenRepository;
import com.cw.scheduler.service.interfaces.CalendarIntegrationService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/google/calendar")
@RequiredArgsConstructor
public class GoogleCalendarController {

    private final GoogleAuthorizationCodeFlow googleFlow;
    private final AppointmentRepository appointmentRepository;
    private final CalendarIntegrationService calendarIntegrationService;
    private final GoogleCalendarTokenRepository tokenRepository;

    @GetMapping("/callback")
    public void oauthCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String id,
            HttpServletResponse response) throws Exception {

        Long appointmentId = Long.parseLong(id);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        User user = appointment.getUser();

        TokenResponse tokenResponse = googleFlow.newTokenRequest(code)
                .setRedirectUri("http://localhost:8085/api/v1/google/calendar/callback")
                .execute();

        Credential credential = googleFlow.createAndStoreCredential(tokenResponse, String.valueOf(user.getId()));

        // Store in DB for reuse
        GoogleCalendarToken tokenEntity = new GoogleCalendarToken(
                user.getId(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresInSeconds() != null ? System.currentTimeMillis() + tokenResponse.getExpiresInSeconds() * 1000 : 0
        );
        tokenRepository.save(tokenEntity);

        // Add event
        String eventId = calendarIntegrationService.addEventToCalendar(appointment, user, credential);
        appointment.setCalendarEventId(eventId);
        appointmentRepository.save(appointment);

        response.sendRedirect("http://localhost:8085/api/v1/appointments/my");
    }
}