package com.cw.scheduler.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProviderResponseDTO {

    private Long id;
    private Long userId;
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

    private LocalDateTime applicationDate;
}
