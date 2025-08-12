package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ProviderRejectionRequestDTO;
import com.cw.scheduler.dto.response.ServiceProviderResponseDTO;

import java.util.List;

public interface AdminProviderService {
    ApiResponse<String> approveProviderRequest(Long userId);
    ApiResponse<String> rejectProviderRequest(ProviderRejectionRequestDTO request);
    ApiResponse<List<ServiceProviderResponseDTO>> getPendingProviderApplications(int page, int size);
    ApiResponse<List<ServiceProviderResponseDTO>> getAllApprovedServiceProviders();
}
