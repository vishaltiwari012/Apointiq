package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ServiceProviderRequestDTO;
import com.cw.scheduler.dto.response.ServiceProviderResponseDTO;
import com.cw.scheduler.entity.ServiceProvider;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.entity.enums.ApplicationStatus;
import com.cw.scheduler.exception.BadRequestException;
import com.cw.scheduler.exception.UserNotFoundException;
import com.cw.scheduler.repository.ServiceProviderRepository;
import com.cw.scheduler.security.AuthenticationFacade;
import com.cw.scheduler.service.interfaces.ServiceProviderService;
import com.cw.scheduler.util.CloudinaryUtil;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ServiceProviderServiceImpl implements ServiceProviderService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final ModelMapper modelMapper;
    private final AuthenticationFacade authenticationFacade;
    private final CloudinaryUtil cloudinaryUtil;

    @Override
    public ApiResponse<String> apply(ServiceProviderRequestDTO request, User user){
        if(serviceProviderRepository.existsByUser(user)) {
            throw new BadRequestException("You have already applied or are already a provider.");
        }
        ServiceProvider serviceProvider = modelMapper.map(request, ServiceProvider.class);
        serviceProvider.setApplicationStatus(ApplicationStatus.PENDING);
        serviceProvider.setUser(user);

        serviceProviderRepository.save(serviceProvider);

        return ApiResponse.success("Application submitted. Please upload documents.");
    }

    @Override
    public ApiResponse<ServiceProviderResponseDTO> getMyProviderProfile() {
        User user = authenticationFacade.getCurrentUser();
        ServiceProvider provider = user.getServiceProvider();

        if (provider == null) {
            throw new BadRequestException("You are not a registered service provider.");
        }

        ServiceProviderResponseDTO response = modelMapper.map(provider, ServiceProviderResponseDTO.class);
        return ApiResponse.success(response, "Service Provider profile fetched successfully.");
    }

    @Override
    public ApiResponse<ServiceProviderResponseDTO> getApplicationByUser(User user) {
        ServiceProvider provider = serviceProviderRepository.findByUser(user)
                .orElseThrow(() -> new UserNotFoundException("No service provider application found for user"));

        ServiceProviderResponseDTO response = modelMapper.map(provider, ServiceProviderResponseDTO.class);
        return ApiResponse.success(response, "User application for service provider");
    }

    @Override
    public ApiResponse<String> uploadDocuments(User user, MultipartFile profilePicture, MultipartFile idProof, MultipartFile licenseDoc) throws FileUploadException {
        try {
            ServiceProvider serviceProvider = serviceProviderRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Service provider application not found"));

            // Upload profile picture
            Map<String, Object> profileResult = cloudinaryUtil.uploadFile(profilePicture, user.getId() + "/profile");
            String profilePicUrl = cloudinaryUtil.getSecureUrl(profileResult);
            String profilePicPublicId = cloudinaryUtil.getPublicId(profileResult);

            // Upload ID proof
            Map<String, Object> idProofResult = cloudinaryUtil.uploadFile(idProof, user.getId() + "/id-proof");
            String idProofUrl = cloudinaryUtil.getSecureUrl(idProofResult);
            String idProofPublicId = cloudinaryUtil.getPublicId(idProofResult);

            // Upload license document
            Map<String, Object> licenseResult = cloudinaryUtil.uploadFile(licenseDoc, user.getId() + "/license");
            String licenseUrl = cloudinaryUtil.getSecureUrl(licenseResult);
            String licensePublicId = cloudinaryUtil.getPublicId(licenseResult);

            // Set Cloudinary URLs and public IDs
            serviceProvider.setProfilePictureUrl(profilePicUrl);
            serviceProvider.setIdProofDocumentUrl(idProofUrl);
            serviceProvider.setLicenseDocumentUrl(licenseUrl);
            serviceProvider.setProfilePicturePublicId(profilePicPublicId);
            serviceProvider.setIdProofDocumentPublicId(idProofPublicId);
            serviceProvider.setLicenseDocumentPublicId(licensePublicId);

            serviceProviderRepository.save(serviceProvider);

            return ApiResponse.success("Documents uploaded successfully");
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload one or more documents. " + e.getMessage());
        }

    }

    @Override
    public ApiResponse<String> getApplicationStatus(User user) {
        ServiceProvider provider = user.getServiceProvider();
        if (provider == null) {
            throw new BadRequestException("You have not applied for service provider yet.");
        }

        String status = provider.getApplicationStatus().name();

        return ApiResponse.success("Your application status is: " + status);
    }

}
