package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.entity.Appointment;
import com.cw.scheduler.entity.User;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;

import java.io.IOException;

public interface CalendarIntegrationService {
    String addEventToCalendar(Appointment appointment, User user, Credential credential);
    void updateEventInCalendar(Appointment appointment, String eventId, User user, Credential credential);
    public void deleteEventFromCalendar(String eventId, User user, Credential credential);
    Credential getCredentialForUser(User user, GoogleAuthorizationCodeFlow googleFlow) throws IOException;
}
