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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferingServiceImpl implements OfferingService {

    private final OfferedServiceRepository offeredServiceRepository;
    private final CategoryRepository categoryRepository;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "providerOfferedServices", key = "@authenticationFacade.getCurrentUser().serviceProvider.id"),
            @CacheEvict(value = "allOfferedServices", allEntries = true)
    })
    public ApiResponse<OfferingServiceResponseDTO> createService(CreateOfferingServiceRequestDTO request) {
        ServiceProvider provider = getCurrentServiceProvider();
        log.info("Creating service for providerId={} with name={}", provider.getId(), request.getName());

        int count = offeredServiceRepository.countByProvider(provider);
        if (count >= 2) {
            log.warn("ProviderId={} already has maximum allowed services", provider.getId());
            throw new BadRequestException("You can only create up to 2 services.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Category not found with id={}", request.getCategoryId());
                    return new ResourceNotFoundException("Category not found");
                });

        OfferedService offeringService = new OfferedService();
        offeringService.setName(request.getName());
        offeringService.setDescription(request.getDescription());
        offeringService.setCategory(category);
        offeringService.setProvider(provider);

        OfferedService saved = offeredServiceRepository.save(offeringService);
        log.debug("Service saved with id={} for providerId={}", saved.getId(), provider.getId());

        return ApiResponse.success(toDto(saved), "Service created by Provider successfully.");
    }

    @Override
    @Cacheable(value = "providerOfferedServices", key = "@authenticationFacade.getCurrentUser().serviceProvider.id")
    public ApiResponse<List<OfferingServiceResponseDTO>> getServicesByProvider() {
        ServiceProvider provider = getCurrentServiceProvider();
        log.info("Fetching offered services for providerId={}", provider.getId());

        List<OfferingServiceResponseDTO> response = offeredServiceRepository.findByProvider(provider)
                .stream()
                .map(this::toDto)
                .toList();

        log.debug("Found {} services for providerId={}", response.size(), provider.getId());
        return ApiResponse.success(response, "Services offered by current provider " + provider.getFullName());
    }

    @Override
    @Cacheable(value = "allOfferedServices", key = "'page:' + #page + ':size:' + #size")
    public ApiResponse<Page<OfferingServiceResponseDTO>> getAllServices(int page, int size) {
        log.info("Fetching all services, page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<OfferedService> servicesPage = offeredServiceRepository.findAll(pageable);

        Page<OfferingServiceResponseDTO> response = servicesPage.map(this::toDto);
        log.debug("Fetched {} services in current page", response.getContent().size());

        return ApiResponse.success(response, "All Services fetched successfully.");
    }

    private ServiceProvider getCurrentServiceProvider() {
        User user = authenticationFacade.getCurrentUser();
        ServiceProvider provider = user.getServiceProvider();

        if (provider == null || provider.getApplicationStatus() != ApplicationStatus.APPROVED) {
            log.warn("UserId={} attempted to access provider-only functionality without approval", user.getId());
            throw new BadRequestException("You are not an approved service provider.");
        }
        return provider;
    }

    private OfferingServiceResponseDTO toDto(OfferedService service) {
        return new OfferingServiceResponseDTO(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getCategory().getName()
        );
    }
}
