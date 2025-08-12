package com.cw.scheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "google_calendar_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCalendarToken {

    @Id
    private Long userId; // same as our User table

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private Long tokenExpiryMillis;
}
