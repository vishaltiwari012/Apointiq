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
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final IndividualServiceRepository individualServiceRepository;
    private final ProviderScheduleRepository providerScheduleRepository;
    private final AuthenticationFacade authenticationFacade;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<AppointmentResponseDTO> bookAppointment(AppointmentRequestDTO requestDTO) {
        User currentUser = authenticationFacade.getCurrentUser();

        IndividualService service = individualServiceRepository.findById(requestDTO.getIndividualServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Particular Service not found"));

        ServiceProvider provider = service.getOfferedService().getProvider();

        LocalDateTime appointmentTime = requestDTO.getAppointmentTime();
        DayOfWeek dayOfWeek = appointmentTime.getDayOfWeek();
        LocalTime time = appointmentTime.toLocalTime();

        // Check provider availability
        Optional<ProviderSchedule> scheduleOpt =
                providerScheduleRepository.isProviderAvailableAt(provider.getId(), dayOfWeek, time);
        if (appointmentRepository.existsByProviderIdAndAppointmentTime(provider.getId(), requestDTO.getAppointmentTime())) {
            throw new BadRequestException("The provider already has an appointment at this time.");
        }

        Appointment appointment = new Appointment();
        appointment.setUser(currentUser);
        appointment.setIndividualService(service);
        appointment.setProvider(provider);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus(AppointmentStatus.PENDING);

        Appointment saved = appointmentRepository.save(appointment);

        return ApiResponse.success(toDTO(saved), "Appointment booked successfully.");
    }

    @Override
    public ApiResponse<List<AppointmentResponseDTO>> getAppointmentsForCurrentUser() {
        User user = authenticationFacade.getCurrentUser();

        List<Appointment> appointments = appointmentRepository.findByUserId(user.getId());

        return ApiResponse.success(appointments.stream().map(this::toDTO).toList(), "User's appointments retrieved.");
    }

    @Override
    public ApiResponse<String> cancelAppointment(Long appointmentId) {
        User user = authenticationFacade.getCurrentUser();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Unauthorized to cancel this appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        return ApiResponse.success("Appointment cancelled successfully.");
    }

    @Override
    public ApiResponse<List<AppointmentResponseDTO>> getProviderAppointments() {
        ServiceProvider provider = authenticationFacade.getCurrentUser().getServiceProvider();
        List<Appointment> appointments = appointmentRepository.findByProviderId(provider.getId());

        return ApiResponse.success(
                appointments.stream().map(this::toDTO).toList(),
                "Fetched provider appointments."
        );
    }

    @Override
    public ApiResponse<List<AppointmentResponseDTO>> getUpcomingAppointments() {
        ServiceProvider provider = authenticationFacade.getCurrentUser().getServiceProvider();
        List<Appointment> appointments = appointmentRepository.findUpcomingAppointmentsForProvider(provider.getId(), LocalDateTime.now());
        return ApiResponse.success(
                appointments.stream().map(this::toDTO).toList(),
                "Fetched upcoming appointments."
        );
    }

    @Override
    public ApiResponse<AppointmentResponseDTO> updateAppointmentStatus(Long appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Ensure only provider can update
        ServiceProvider currentProvider = authenticationFacade.getCurrentUser().getServiceProvider();
        if (!appointment.getProvider().getId().equals(currentProvider.getId())) {
            throw new RuntimeException("You are not authorized to update this appointment.");
        }

        appointment.setStatus(status);
        Appointment updated = appointmentRepository.save(appointment);

        return ApiResponse.success(toDTO(updated), "Appointment status updated to " + status.name());
    }

    @Override
    public ApiResponse<List<AppointmentResponseDTO>> getAppointmentsForDate(LocalDate date) {
        ServiceProvider provider = authenticationFacade.getCurrentUser().getServiceProvider();

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> appointments = appointmentRepository
                .findByProviderIdAndAppointmentTimeBetween(provider.getId(), startOfDay, endOfDay);

        List<AppointmentResponseDTO> response = appointments.stream()
                .map(this::toDTO)
                .toList();

        return ApiResponse.success(response, "Appointments for date: " + date);
    }

    private AppointmentResponseDTO toDTO(Appointment appointment) {
        AppointmentResponseDTO dto = modelMapper.map(appointment, AppointmentResponseDTO.class);

        dto.setIndividualServiceName(appointment.getIndividualService().getName());
        dto.setProviderFullName(appointment.getProvider().getFullName());

        return dto;
    }
}
