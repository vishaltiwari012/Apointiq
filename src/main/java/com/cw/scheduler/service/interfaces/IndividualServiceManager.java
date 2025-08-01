package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.CreateIndividualServiceRequestDTO;
import com.cw.scheduler.dto.response.IndividualServiceResponseDTO;

import java.util.List;

public interface IndividualServiceManager {
    ApiResponse<IndividualServiceResponseDTO> createIndividualService(Long offeredServiceId, CreateIndividualServiceRequestDTO request);
    ApiResponse<List<IndividualServiceResponseDTO>> getIndividualServicesByOfferedService(Long offeredServiceId);
}
