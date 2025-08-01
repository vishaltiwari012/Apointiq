package com.cw.scheduler.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "individual_services")
public class IndividualService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Example - Haircut, massage
    private String description;
    private int durationMinutes;
    private double price;

    @ManyToOne
    @JoinColumn(name = "offered_service_id")
    private OfferedService offeredService;

    @OneToMany(mappedBy = "individualService")
    private List<Appointment> appointments;
}
