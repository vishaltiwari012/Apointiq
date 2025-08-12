package com.cw.scheduler.service.impl;

import com.cw.scheduler.entity.Appointment;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.repository.GoogleCalendarTokenRepository;
import com.cw.scheduler.service.interfaces.CalendarIntegrationService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleCalendarServiceImpl implements CalendarIntegrationService {

    private final GoogleCalendarTokenRepository tokenRepository;

    @Override
    public String addEventToCalendar(Appointment appointment, User user, Credential credential) {
        try {
            Calendar calendar = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("Appointment Scheduler").build();

            // Convert LocalDateTime to RFC3339 format with timezone
            ZonedDateTime startZdt = appointment.getAppointmentTime().atZone(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime endZdt = startZdt.plusMinutes(appointment.getIndividualService().getDurationMinutes());

            Event event = new Event()
                    .setSummary("Appointment: " + appointment.getIndividualService().getName())
                    .setDescription("With " + appointment.getProvider().getBusinessName())
                    .setStart(new EventDateTime()
                            .setDateTime(new DateTime(startZdt.toInstant().toEpochMilli()))
                            .setTimeZone("Asia/Kolkata"))
                    .setEnd(new EventDateTime()
                            .setDateTime(new DateTime(endZdt.toInstant().toEpochMilli()))
                            .setTimeZone("Asia/Kolkata"))
                    .setAttendees(List.of(
                            new EventAttendee().setEmail(user.getEmail()),
                            new EventAttendee().setEmail(appointment.getProvider().getUser().getEmail())
                    ));

            Event createdEvent = calendar.events().insert("primary", event).execute();
            return createdEvent.getId();

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to create Google Calendar event", e);
        }
    }
    @Override
    public Credential getCredentialForUser(User user, GoogleAuthorizationCodeFlow googleFlow) throws IOException {
        return googleFlow.loadCredential(String.valueOf(user.getId()));
    }
    @Override
    public void updateEventInCalendar(Appointment appointment, String eventId, User user, Credential credential) {
        // Optional for rescheduling
    }

    @Override
    public void deleteEventFromCalendar(String eventId, User user, Credential credential) {
        try {
            Calendar calendar = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("Appointment Scheduler").build();

            calendar.events().delete("primary", eventId).execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete Google Calendar event", e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
