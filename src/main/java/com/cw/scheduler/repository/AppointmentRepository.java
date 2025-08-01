package com.cw.scheduler.repository;

import com.cw.scheduler.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    // Find appointments by user (customer)
    List<Appointment> findByUserId(Long userId);

    // Find appointments by provider
    List<Appointment> findByProviderId(Long providerId);

    // Find appointments for a given provider and date
    List<Appointment> findByProviderIdAndAppointmentTimeBetween(Long providerId, LocalDateTime start, LocalDateTime end);

    // Check for conflict before booking
    boolean existsByProviderIdAndAppointmentTime(Long providerId, LocalDateTime appointmentTime);

    //Find upcoming appointments
    @Query("SELECT a FROM Appointment a WHERE a.provider.id = :providerId AND a.appointmentTime >= :now ORDER BY a.appointmentTime ASC")
    List<Appointment> findUpcomingAppointmentsForProvider(@Param("providerId") Long providerId, @Param("now") LocalDateTime now);
}

