package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.CreateOfferingServiceRequestDTO;
import com.cw.scheduler.dto.response.OfferingServiceResponseDTO;
import com.cw.scheduler.entity.Category;
import com.cw.scheduler.entity.OfferedService;
import com.cw.scheduler.entity.ServiceProvider;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.entity.enums.ApplicationStatus;
import com.cw.scheduler.exception.BadRequestException;
import com.cw.scheduler.exception.ResourceNotFoundException;
import com.cw.scheduler.repository.CategoryRepository;
import com.cw.scheduler.repository.OfferedServiceRepository;
import com.cw.scheduler.security.AuthenticationFacade;
import com.cw.scheduler.service.interfaces.OfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferingServiceImpl implements OfferingService {

    private final OfferedServiceRepository offeredServiceRepository;
    private final CategoryRepository categoryRepository;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public ApiResponse<OfferingServiceResponseDTO> createService(CreateOfferingServiceRequestDTO request) {
        ServiceProvider provider = getCurrentServiceProvider();

        int count = offeredServiceRepository.countByProvider(provider);
        if (count >= 2) {
            throw new BadRequestException("You can only create up to 2 services.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        OfferedService offeringService = new OfferedService();
        offeringService.setName(request.getName());
        offeringService.setDescription(request.getDescription());
        offeringService.setCategory(category);
        offeringService.setProvider(provider);

        OfferedService saved = offeredServiceRepository.save(offeringService);

        OfferingServiceResponseDTO response = new OfferingServiceResponseDTO(
                saved.getId(), saved.getName(), saved.getDescription(), saved.getCategory().getName()
        );

        return ApiResponse.success(response, "Service created by Provider successfully.");
    }

    @Override
    public ApiResponse<List<OfferingServiceResponseDTO>> getServicesByProvider() {
        ServiceProvider provider = getCurrentServiceProvider();
        List<OfferedService> offeredServices = offeredServiceRepository.findByProvider(provider);

        List<OfferingServiceResponseDTO> response = offeredServices.stream().map(s ->
                new OfferingServiceResponseDTO(
                        s.getId(), s.getName(),
                        s.getDescription(),
                        s.getCategory().getName())
        ).collect(Collectors.toList());

        return ApiResponse.success(response, "Services offered by current provider " + provider.getFullName());

    }

    @Override
    public ApiResponse<Page<OfferingServiceResponseDTO>> getAllServices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<OfferedService> servicesPage = offeredServiceRepository.findAll(pageable);

        Page<OfferingServiceResponseDTO> response = servicesPage.map(service ->
                new OfferingServiceResponseDTO(service.getId(), service.getName(),
                        service.getDescription(), service.getCategory().getName())
        );

        return ApiResponse.success(response, "All Services fetched successfully.");
    }

    private ServiceProvider getCurrentServiceProvider() {
        User user = authenticationFacade.getCurrentUser();
        ServiceProvider provider = user.getServiceProvider();

        if (provider == null || provider.getApplicationStatus() != ApplicationStatus.APPROVED) {
            throw new BadRequestException("You are not a approved service providers.");
        }
        return provider;
    }
}
