package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ServiceProviderRequestDTO;
import com.cw.scheduler.dto.response.ServiceProviderResponseDTO;
import com.cw.scheduler.entity.ServiceProvider;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.entity.enums.ApplicationStatus;
import com.cw.scheduler.exception.BadRequestException;
import com.cw.scheduler.exception.ResourceNotFoundException;
import com.cw.scheduler.exception.UserNotFoundException;
import com.cw.scheduler.repository.ServiceProviderRepository;
import com.cw.scheduler.security.AuthenticationFacade;
import com.cw.scheduler.service.interfaces.ServiceProviderService;
import com.cw.scheduler.util.CloudinaryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceProviderServiceImpl implements ServiceProviderService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final ModelMapper modelMapper;
    private final AuthenticationFacade authenticationFacade;
    private final CloudinaryUtil cloudinaryUtil;

    @Override
    public ApiResponse<String> apply(ServiceProviderRequestDTO request, User user){
        log.info("UserId={} applying for Service Provider", user.getId());

        if(serviceProviderRepository.existsByUser(user)) {
            log.warn("Duplicate application attempt by userId={}", user.getId());
            throw new BadRequestException("You have already applied or are already a provider.");
        }

        ServiceProvider serviceProvider = modelMapper.map(request, ServiceProvider.class);
        serviceProvider.setApplicationStatus(ApplicationStatus.PENDING);
        serviceProvider.setUser(user);

        serviceProviderRepository.save(serviceProvider);
        log.debug("Service Provider application saved for userId={}", user.getId());

        return ApiResponse.success("Application submitted. Please upload documents.");
    }

    @Override
    @Cacheable(value = "providerProfiles", key = "@authenticationFacade.getCurrentUserId()")
    public ApiResponse<ServiceProviderResponseDTO> getMyProviderProfile() {
        Long userId = authenticationFacade.getCurrentUser().getId();
        log.info("Fetching provider profile for userId={}", userId);

        ServiceProvider provider = authenticationFacade.getCurrentUser().getServiceProvider();
        if (provider == null) {
            log.warn("No provider profile found for userId={}", userId);
            throw new BadRequestException("You are not a registered service provider.");
        }

        ServiceProviderResponseDTO response = modelMapper.map(provider, ServiceProviderResponseDTO.class);
        return ApiResponse.success(response, "Service Provider profile fetched successfully.");
    }

    @Override
    @Cacheable(value = "providerApplications", key = "#user.id")
    public ApiResponse<ServiceProviderResponseDTO> getApplicationByUser(User user) {
        log.info("Fetching provider application for userId={}", user.getId());

        ServiceProvider provider = serviceProviderRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.warn("No provider application found for userId={}", user.getId());
                    return new UserNotFoundException("No service provider application found for user");
                });

        ServiceProviderResponseDTO response = modelMapper.map(provider, ServiceProviderResponseDTO.class);
        return ApiResponse.success(response, "User application for service provider");
    }

    @Override
    @CachePut(value = "providerProfiles", key = "#user.id")
    @CacheEvict(value = "providerApplications", key = "#user.id")
    public ApiResponse<String> uploadDocuments(User user, MultipartFile profilePicture, MultipartFile idProof, MultipartFile licenseDoc) throws FileUploadException {
        log.info("Uploading provider documents for userId={}", user.getId());

        try {
            ServiceProvider serviceProvider = serviceProviderRepository.findByUser(user)
                    .orElseThrow(() -> {
                        log.error("Service provider application not found for userId={}", user.getId());
                        return new ResourceNotFoundException("Service provider application not found");
                    });

            uploadAndSetProviderDocs(serviceProvider, user, profilePicture, idProof, licenseDoc);

            serviceProviderRepository.save(serviceProvider);
            log.debug("Documents saved for userId={}", user.getId());

            return ApiResponse.success("Documents uploaded successfully");
        } catch (IOException e) {
            log.error("Document upload failed for userId={} - {}", user.getId(), e.getMessage());
            throw new FileUploadException("Failed to upload one or more documents. " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> getApplicationStatus(User user) {
        log.info("Fetching application status for userId={}", user.getId());

        ServiceProvider provider = user.getServiceProvider();
        if (provider == null) {
            log.warn("UserId={} has not applied for service provider", user.getId());
            throw new BadRequestException("You have not applied for service provider yet.");
        }

        String status = provider.getApplicationStatus().name();
        return ApiResponse.success("Your application status is: " + status);
    }


    /**
     * Private helper to handle Cloudinary uploads and update ServiceProvider entity.
     */
    private void uploadAndSetProviderDocs(ServiceProvider provider, User user, MultipartFile profilePicture, MultipartFile idProof, MultipartFile licenseDoc) throws IOException {

        Map<String, Object> profileResult = cloudinaryUtil.uploadFile(profilePicture, user.getId() + "/profile");
        provider.setProfilePictureUrl(cloudinaryUtil.getSecureUrl(profileResult));
        provider.setProfilePicturePublicId(cloudinaryUtil.getPublicId(profileResult));

        Map<String, Object> idProofResult = cloudinaryUtil.uploadFile(idProof, user.getId() + "/id-proof");
        provider.setIdProofDocumentUrl(cloudinaryUtil.getSecureUrl(idProofResult));
        provider.setIdProofDocumentPublicId(cloudinaryUtil.getPublicId(idProofResult));

        Map<String, Object> licenseResult = cloudinaryUtil.uploadFile(licenseDoc, user.getId() + "/license");
        provider.setLicenseDocumentUrl(cloudinaryUtil.getSecureUrl(licenseResult));
        provider.setLicenseDocumentPublicId(cloudinaryUtil.getPublicId(licenseResult));
    }

}
