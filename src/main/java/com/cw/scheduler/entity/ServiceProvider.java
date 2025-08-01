package com.cw.scheduler.entity;

import com.cw.scheduler.entity.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "service_providers")
public class ServiceProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String phoneNumber;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String businessName;
    private String specialization;
    private String qualification;
    private int yearsOfExperience;
    private String licenseNumber;
    private String description;
    private String availabilityNote;

    private String profilePictureUrl;
    private String profilePicturePublicId;

    private String idProofDocumentUrl;
    private String idProofDocumentPublicId;

    private String licenseDocumentUrl;
    private String licenseDocumentPublicId;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;

    private double rating = 0.0;
    private int totalAppointments = 0;

    private String rejectionReason;

    private LocalDateTime applicationDate = LocalDateTime.now();
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime lastUpdated;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfferedService> services = new ArrayList<>();
}
