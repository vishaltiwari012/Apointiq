package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ProviderRejectionRequestDTO;
import com.cw.scheduler.dto.response.ServiceProviderResponseDTO;
import org.springframework.data.domain.Page;

public interface AdminProviderService {
    ApiResponse<String> approveProviderRequest(Long userId);
    ApiResponse<String> rejectProviderRequest(ProviderRejectionRequestDTO request);
    ApiResponse<Page<ServiceProviderResponseDTO>> getPendingProviderApplications(int page, int size);

}
