package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.CreateIndividualServiceRequestDTO;
import com.cw.scheduler.dto.response.IndividualServiceResponseDTO;
import com.cw.scheduler.entity.IndividualService;
import com.cw.scheduler.entity.OfferedService;
import com.cw.scheduler.exception.ResourceNotFoundException;
import com.cw.scheduler.repository.IndividualServiceRepository;
import com.cw.scheduler.repository.OfferedServiceRepository;
import com.cw.scheduler.service.interfaces.IndividualServiceManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndividualServiceManagerImpl implements IndividualServiceManager {

    private final IndividualServiceRepository individualServiceRepository;
    private final OfferedServiceRepository offeredServiceRepository;

    @Override
    public ApiResponse<IndividualServiceResponseDTO> createIndividualService(Long offeredServiceId, CreateIndividualServiceRequestDTO request) {
        OfferedService offeredService = offeredServiceRepository.findById(offeredServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Offered service not found"));

        IndividualService service = new IndividualService();
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setPrice(request.getPrice());
        service.setOfferedService(offeredService);

        IndividualService saved = individualServiceRepository.save(service);

        IndividualServiceResponseDTO response = new IndividualServiceResponseDTO(
                saved.getId(), saved.getName(), saved.getDescription(), saved.getDurationMinutes(), saved.getPrice()
        );

        return ApiResponse.success(response, "Individual service created.");
    }

    @Override
    public ApiResponse<List<IndividualServiceResponseDTO>> getIndividualServicesByOfferedService(Long offeredServiceId) {
        List<IndividualService> services = individualServiceRepository.findByOfferedService_Id(offeredServiceId);

        List<IndividualServiceResponseDTO> response = services.stream().map(s ->
                new IndividualServiceResponseDTO(s.getId(), s.getName(), s.getDescription(),
                        s.getDurationMinutes(), s.getPrice())
        ).collect(Collectors.toList());

        return ApiResponse.success(response, "Fetched all individual services.");
    }
}
