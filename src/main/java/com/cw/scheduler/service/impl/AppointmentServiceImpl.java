package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.AppointmentRequestDTO;
import com.cw.scheduler.dto.response.AppointmentResponseDTO;
import com.cw.scheduler.entity.*;
import com.cw.scheduler.entity.enums.AppointmentStatus;
import com.cw.scheduler.exception.BadRequestException;
import com.cw.scheduler.exception.ResourceNotFoundException;
import com.cw.scheduler.repository.AppointmentRepository;
import com.cw.scheduler.repository.IndividualServiceRepository;
import com.cw.scheduler.repository.ProviderScheduleRepository;
import com.cw.scheduler.security.AuthenticationFacade;
import com.cw.scheduler.service.interfaces.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final IndividualServiceRepository individualServiceRepository;
    private final ProviderScheduleRepository providerScheduleRepository;
    private final AuthenticationFacade authenticationFacade;
    private final ModelMapper modelMapper;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "userAppointments", key = "@authenticationFacade.getCurrentUser().serviceProvider.id"),
            @CacheEvict(value = "providerAppointments", key = "#result.providerId"),
            @CacheEvict(value = "upcomingAppointments", key = "#result.providerId"),
            @CacheEvict(value = "appointmentsByDate", key = "#result.providerId + '_' + #requestDTO.appointmentTime.toLocalDate()")
    })
    public ApiResponse<AppointmentResponseDTO> bookAppointment(AppointmentRequestDTO requestDTO) {
        User currentUser = authenticationFacade.getCurrentUser();
        log.info("Booking appointment for userId={} at {}", currentUser.getId(), requestDTO.getAppointmentTime());

        IndividualService service = individualServiceRepository.findById(requestDTO.getIndividualServiceId())
                .orElseThrow(() -> {
                    log.warn("Service not found with id={}", requestDTO.getIndividualServiceId());
                    return new ResourceNotFoundException("Particular Service not found");
                });

        ServiceProvider provider = service.getOfferedService().getProvider();
        validateProviderAvailability(provider.getId(), requestDTO.getAppointmentTime());

        Appointment appointment = new Appointment();
        appointment.setUser(currentUser);
        appointment.setIndividualService(service);
        appointment.setProvider(provider);
        appointment.setAppointmentTime(requestDTO.getAppointmentTime());
        appointment.setStatus(AppointmentStatus.PENDING);

        Appointment saved = appointmentRepository.save(appointment);

        log.info("Appointment booked successfully: appointmentId={}", saved.getId());
        return ApiResponse.success(toDTO(saved), "Appointment booked successfully.");
    }

    @Override
    @Cacheable(value = "userAppointments", key = "@authenticationFacade.getCurrentUserId()")
    public ApiResponse<List<AppointmentResponseDTO>> getAppointmentsForCurrentUser() {
        User user = authenticationFacade.getCurrentUser();
        log.debug("Fetching appointments for userId={}", user.getId());

        List<Appointment> appointments = appointmentRepository.findByUserId(user.getId());
        return ApiResponse.success(appointments.stream().map(this::toDTO).toList(),
                "User's appointments retrieved.");
    }

    @Override
    public ApiResponse<String> cancelAppointment(Long appointmentId) {
        User user = authenticationFacade.getCurrentUser();
        log.info("UserId={} attempting to cancel appointmentId={}", user.getId(), appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized cancel attempt by userId={} for appointmentId={}", user.getId(), appointmentId);
            throw new BadRequestException("Unauthorized to cancel this appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        log.info("Appointment cancelled: appointmentId={}", appointmentId);
        return ApiResponse.success("Appointment cancelled successfully.");
    }

    @Override
    @Cacheable(value = "providerAppointments", key = "@authenticationFacade.getCurrentUser().serviceProvider.id")
    public ApiResponse<List<AppointmentResponseDTO>> getProviderAppointments() {
        ServiceProvider provider = authenticationFacade.getCurrentUser().getServiceProvider();
        log.debug("Fetching appointments for providerId={}", provider.getId());

        List<Appointment> appointments = appointmentRepository.findByProviderId(provider.getId());
        return ApiResponse.success(appointments.stream().map(this::toDTO).toList(),
                "Fetched provider appointments.");
    }

    @Override
    @Cacheable(value = "upcomingAppointments", key = "@authenticationFacade.getCurrentUser().serviceProvider.id")
    public ApiResponse<List<AppointmentResponseDTO>> getUpcomingAppointments() {
        ServiceProvider provider = authenticationFacade.getCurrentUser().getServiceProvider();
        log.debug("Fetching upcoming appointments for providerId={}", provider.getId());

        List<Appointment> appointments = appointmentRepository.findUpcomingAppointmentsForProvider(
                provider.getId(), LocalDateTime.now());
        return ApiResponse.success(appointments.stream().map(this::toDTO).toList(),
                "Fetched upcoming appointments.");
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "userAppointments", key = "@authenticationFacade.getCurrentUser().serviceProvider.id"),
            @CacheEvict(value = "providerAppointments", key = "#result.providerId"),
            @CacheEvict(value = "upcomingAppointments", key = "#result.providerId"),
            @CacheEvict(value = "appointmentsByDate", allEntries = true)
    })
    public ApiResponse<AppointmentResponseDTO> updateAppointmentStatus(Long appointmentId, AppointmentStatus status) {
        ServiceProvider currentProvider = authenticationFacade.getCurrentUser().getServiceProvider();
        log.info("ProviderId={} updating appointmentId={} to status={}",
                currentProvider.getId(), appointmentId, status);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getProvider().getId().equals(currentProvider.getId())) {
            log.warn("Unauthorized update attempt by providerId={} for appointmentId={}",
                    currentProvider.getId(), appointmentId);
            throw new BadRequestException("You are not authorized to update this appointment.");
        }

        appointment.setStatus(status);
        Appointment updated = appointmentRepository.save(appointment);

        return ApiResponse.success(toDTO(updated), "Appointment status updated to " + status.name());
    }

    @Override
    @Cacheable(value = "appointmentsByDate", key = "@authenticationFacade.getCurrentUser().serviceProvider.id + '_' + #date")
    public ApiResponse<List<AppointmentResponseDTO>> getAppointmentsForDate(LocalDate date) {
        ServiceProvider provider = authenticationFacade.getCurrentUser().getServiceProvider();
        log.debug("Fetching appointments for providerId={} on date={}", provider.getId(), date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> appointments = appointmentRepository
                .findByProviderIdAndAppointmentTimeBetween(provider.getId(), startOfDay, endOfDay);

        return ApiResponse.success(appointments.stream().map(this::toDTO).toList(),
                "Appointments for date: " + date);
    }

    private AppointmentResponseDTO toDTO(Appointment appointment) {
        AppointmentResponseDTO dto = modelMapper.map(appointment, AppointmentResponseDTO.class);
        dto.setIndividualServiceName(appointment.getIndividualService().getName());
        dto.setProviderFullName(appointment.getProvider().getFullName());
        dto.setProviderId(appointment.getProvider().getId());
        dto.setUserId(appointment.getUser().getId());

        return dto;
    }

    private void validateProviderAvailability(Long providerId, LocalDateTime appointmentTime) {
        DayOfWeek dayOfWeek = appointmentTime.getDayOfWeek();
        LocalTime time = appointmentTime.toLocalTime();

        boolean available = providerScheduleRepository.isProviderAvailableAt(providerId, dayOfWeek, time).isPresent();
        if (!available) {
            log.warn("ProviderId={} not available at {}", providerId, appointmentTime);
            throw new BadRequestException("The provider is not available at the requested time.");
        }

        boolean alreadyBooked = appointmentRepository.existsByProviderIdAndAppointmentTime(providerId, appointmentTime);
        if (alreadyBooked) {
            log.warn("ProviderId={} already booked at {}", providerId, appointmentTime);
            throw new BadRequestException("The provider already has an appointment at this time.");
        }
    }
}
