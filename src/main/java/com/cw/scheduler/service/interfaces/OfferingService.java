package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.CreateOfferingServiceRequestDTO;
import com.cw.scheduler.dto.response.OfferingServiceResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OfferingService {
    ApiResponse<OfferingServiceResponseDTO> createService(CreateOfferingServiceRequestDTO request);
    ApiResponse<List<OfferingServiceResponseDTO>> getServicesByProvider();
    ApiResponse<Page<OfferingServiceResponseDTO>> getAllServices(int page, int size);
}
