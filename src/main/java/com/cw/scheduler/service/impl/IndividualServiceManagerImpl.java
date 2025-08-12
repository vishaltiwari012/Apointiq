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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndividualServiceManagerImpl implements IndividualServiceManager {

    private final IndividualServiceRepository individualServiceRepository;
    private final OfferedServiceRepository offeredServiceRepository;

    @Override
    @CacheEvict(value = "individualServicesByOfferedService", key = "#offeredServiceId")
    public ApiResponse<IndividualServiceResponseDTO> createIndividualService(Long offeredServiceId, CreateIndividualServiceRequestDTO request) {
        log.info("Creating individual service for offeredServiceId={}", offeredServiceId);

        OfferedService offeredService = offeredServiceRepository.findById(offeredServiceId)
                .orElseThrow(() -> {
                    log.warn("OfferedService not found with id={}", offeredServiceId);
                    return new ResourceNotFoundException("Offered service not found");
                });

        IndividualService service = new IndividualService();
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setPrice(request.getPrice());
        service.setOfferedService(offeredService);

        IndividualService saved = individualServiceRepository.save(service);
        log.debug("IndividualService saved with id={} for offeredServiceId={}", saved.getId(), offeredServiceId);

        return ApiResponse.success(toDto(saved), "Individual service created.");
    }

    @Override
    @Cacheable(value = "individualServicesByOfferedService", key = "#offeredServiceId")
    public ApiResponse<List<IndividualServiceResponseDTO>> getIndividualServicesByOfferedService(Long offeredServiceId) {
        log.info("Fetching individual services for offeredServiceId={}", offeredServiceId);

        List<IndividualServiceResponseDTO> response = individualServiceRepository.findByOfferedService_Id(offeredServiceId)
                .stream()
                .map(this::toDto)
                .toList();

        log.debug("Found {} individual services for offeredServiceId={}", response.size(), offeredServiceId);
        return ApiResponse.success(response, "Fetched all individual services.");
    }

    private IndividualServiceResponseDTO toDto(IndividualService service) {
        return new IndividualServiceResponseDTO(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.getPrice()
        );
    }
}
