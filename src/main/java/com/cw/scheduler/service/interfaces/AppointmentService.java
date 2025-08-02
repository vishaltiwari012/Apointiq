package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.AppointmentRequestDTO;
import com.cw.scheduler.dto.response.AppointmentResponseDTO;
import com.cw.scheduler.entity.enums.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    ApiResponse<AppointmentResponseDTO> bookAppointment(AppointmentRequestDTO requestDTO);
    ApiResponse<List<AppointmentResponseDTO>> getAppointmentsForCurrentUser();
    ApiResponse<String> cancelAppointment(Long appointmentId);
    ApiResponse<List<AppointmentResponseDTO>> getProviderAppointments();
    ApiResponse<List<AppointmentResponseDTO>> getUpcomingAppointments();
    ApiResponse<AppointmentResponseDTO> updateAppointmentStatus(Long appointmentId, AppointmentStatus status);
    ApiResponse<List<AppointmentResponseDTO>> getAppointmentsForDate(LocalDate date);

}
