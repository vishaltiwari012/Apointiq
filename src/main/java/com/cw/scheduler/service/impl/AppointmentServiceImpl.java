package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.AppointmentRequestDTO;
import com.cw.scheduler.dto.response.AppointmentResponseDTO;
import com.cw.scheduler.entity.Appointment;
import com.cw.scheduler.entity.IndividualService;
import com.cw.scheduler.entity.ServiceProvider;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.entity.enums.AppointmentStatus;
import com.cw.scheduler.entity.enums.NotificationType;
import com.cw.scheduler.exception.BadRequestException;
import com.cw.scheduler.exception.ResourceNotFoundException;
import com.cw.scheduler.repository.AppointmentRepository;
import com.cw.scheduler.repository.IndividualServiceRepository;
import com.cw.scheduler.repository.ProviderScheduleRepository;
import com.cw.scheduler.security.AuthenticationFacade;
import com.cw.scheduler.service.interfaces.AppointmentService;
import com.cw.scheduler.service.interfaces.NotificationService;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final IndividualServiceRepository individualServiceRepository;
    private final ProviderScheduleRepository providerScheduleRepository;
    private final AuthenticationFacade authenticationFacade;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

//    @Override
//    @Caching(evict = {
//            @CacheEvict(value = "userAppointments", key = "@authenticationFacade.getCurrentUser().serviceProvider.id"),
//            @CacheEvict(value = "providerAppointments", key = "#result.providerId"),
//            @CacheEvict(value = "upcomingAppointments", key = "#result.providerId"),
//            @CacheEvict(value = "appointmentsByDate", key = "#result.providerId + '_' + #requestDTO.appointmentTime.toLocalDate()")
//    })
//    public ApiResponse<AppointmentResponseDTO> bookAppointment(AppointmentRequestDTO requestDTO) {
//        User currentUser = authenticationFacade.getCurrentUser();
//        log.info("Booking appointment for userId={} at {}", currentUser.getId(), requestDTO.getAppointmentTime());
//
//        IndividualService service = individualServiceRepository.findById(requestDTO.getIndividualServiceId())
//                .orElseThrow(() -> {
//                    log.warn("Service not found with id={}", requestDTO.getIndividualServiceId());
//                    return new ResourceNotFoundException("Particular Service not found");
//                });
//
//        ServiceProvider provider = service.getOfferedService().getProvider();
//        validateProviderAvailability(provider.getId(), requestDTO.getAppointmentTime());
//
//        Appointment appointment = new Appointment();
//        appointment.setUser(currentUser);
//        appointment.setIndividualService(service);
//        appointment.setProvider(provider);
//        appointment.setAppointmentTime(requestDTO.getAppointmentTime());
//        appointment.setStatus(AppointmentStatus.CONFIRMED);
//
//        Appointment saved = appointmentRepository.save(appointment);
//
//        // Add to Google Calendar
//        String eventId = googleCalendarService.addEventToCalendar(saved, currentUser);
//        saved.setCalendarEventId(eventId);
//        appointmentRepository.save(saved);
//
//        // Send confirmation email & notification
//        sendAppointmentConfirmationEmail(saved);
//
//        log.info("Appointment booked successfully: appointmentId={}", saved.getId());
//        return ApiResponse.success(toDTO(saved), "Appointment booked successfully.");
//    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "userAppointments", key = "@authenticationFacade.getCurrentUser().serviceProvider.id"),
            @CacheEvict(value = "providerAppointments", key = "#result.provider.id"),
            @CacheEvict(value = "upcomingAppointments", key = "#result.provider.id"),
            @CacheEvict(value = "appointmentsByDate",
                    key = "#result.provider.id + '_' + #requestDTO.appointmentTime.toLocalDate()")
    })
    public Appointment bookAppointmentAndReturnEntity(AppointmentRequestDTO requestDTO) {
        User currentUser = authenticationFacade.getCurrentUser();

        IndividualService service = individualServiceRepository.findById(requestDTO.getIndividualServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        ServiceProvider provider = service.getOfferedService().getProvider();
        validateProviderAvailability(provider.getId(), requestDTO.getAppointmentTime());

        Appointment appointment = new Appointment();
        appointment.setUser(currentUser);
        appointment.setIndividualService(service);
        appointment.setProvider(provider);
        appointment.setAppointmentTime(requestDTO.getAppointmentTime());
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        Appointment saved = appointmentRepository.save(appointment);

        sendAppointmentConfirmationEmail(saved);

        return saved;
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

        // Send cancellation email
        sendAppointmentCancellationEmail(appointment);

        log.info("Appointment cancelled: appointmentId={}", appointmentId);
        return ApiResponse.success("Appointment cancelled successfully.");
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

    @Override
    public void save(Appointment appointment) {
        appointmentRepository.save(appointment);
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

    private void sendAppointmentConfirmationEmail(Appointment appointment) {
        User user = appointment.getUser();

        // Send email using HTML template
        notificationService.sendEmail(
                user.getEmail(),
                "Your Appointment is Confirmed!",
                "appointment-confirmation",
                Map.of(
                        "name", user.getName(),
                        "serviceName", appointment.getIndividualService().getName(),
                        "providerName", appointment.getProvider().getBusinessName(),
                        "appointmentDate", appointment.getAppointmentTime().toLocalDate().toString(),
                        "appointmentTime", appointment.getAppointmentTime().toLocalTime().toString(),
                        "dashboardUrl", "http://localhost:8085/user/appointments"
                )
        );

        // Save notification in DB
        notificationService.saveNotification(
                user,
                String.format(
                        "Your appointment for %s with %s on %s at %s is confirmed.",
                        appointment.getIndividualService().getName(),
                        appointment.getProvider().getBusinessName(),
                        appointment.getAppointmentTime().toLocalDate(),
                        appointment.getAppointmentTime().toLocalTime()
                ),
                NotificationType.APPOINTMENT_CONFIRMED
        );
    }

    private void sendAppointmentCancellationEmail(Appointment appointment) {
        User user = appointment.getUser();

        notificationService.sendEmail(
                user.getEmail(),
                "Your Appointment Has Been Cancelled",
                "appointment-cancellation.html", // Thymeleaf template
                Map.of(
                        "name", user.getName(),
                        "serviceName", appointment.getIndividualService().getName(),
                        "providerName", appointment.getProvider().getBusinessName(),
                        "appointmentDate", appointment.getAppointmentTime().toLocalDate().toString(),
                        "appointmentTime", appointment.getAppointmentTime().toLocalTime().toString(),
                        "dashboardUrl", "http://localhost:8085/user/appointments"
                )
        );

        notificationService.saveNotification(
                user,
                String.format(
                        "Your appointment for %s with %s on %s at %s has been cancelled.",
                        appointment.getIndividualService().getName(),
                        appointment.getProvider().getBusinessName(),
                        appointment.getAppointmentTime().toLocalDate(),
                        appointment.getAppointmentTime().toLocalTime()
                ),
                NotificationType.APPOINTMENT_CANCELLED
        );
    }


}
