package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ProviderRejectionRequestDTO;
import com.cw.scheduler.dto.response.ServiceProviderResponseDTO;
import com.cw.scheduler.entity.ServiceProvider;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.entity.enums.ApplicationStatus;
import com.cw.scheduler.entity.enums.NotificationType;
import com.cw.scheduler.exception.BadRequestException;
import com.cw.scheduler.exception.UserNotFoundException;
import com.cw.scheduler.repository.ServiceProviderRepository;
import com.cw.scheduler.repository.UserRepository;
import com.cw.scheduler.service.interfaces.AdminProviderService;
import com.cw.scheduler.service.interfaces.NotificationService;
import com.cw.scheduler.service.interfaces.RoleService;
import com.cw.scheduler.util.CloudinaryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProviderServiceImpl implements AdminProviderService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private final CloudinaryUtil cloudinaryUtil;

    @Override
    public ApiResponse<String> approveProviderRequest(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id : " + userId));

        ServiceProvider provider = getServiceProvider(user);

        provider.setApplicationStatus(ApplicationStatus.APPROVED);
        provider.setApprovedAt(LocalDateTime.now());
        // Clear rejection-related data if present
        provider.setRejectedAt(null);
        provider.setRejectionReason(null);
        serviceProviderRepository.save(provider);

        user.getRoles().add(roleService.getServiceProviderRole());
        userRepository.save(user);

        log.info("Approved service provider request for user: {}", user.getEmail());

        sendApprovalEmail(user);

        return ApiResponse.success("Service provider request approved.");
    }

    private static ServiceProvider getServiceProvider(User user) {
        ServiceProvider provider = user.getServiceProvider();

        if (provider == null) {
            throw new BadRequestException("User has not applied to become a service provider.");
        }

        if (provider.getApplicationStatus() == ApplicationStatus.APPROVED) {
            throw new BadRequestException("User is already an approved service provider.");
        }

        if (provider.getApplicationStatus() != ApplicationStatus.PENDING &&
                provider.getApplicationStatus() != ApplicationStatus.REJECTED) {
            throw new BadRequestException("Invalid application status for approval.");
        }
        return provider;
    }

    @Override
    public ApiResponse<String> rejectProviderRequest(ProviderRejectionRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id : " + request.getUserId()));

        ServiceProvider provider = getProvider(user);
        deleteProviderUploadedDocuments(provider, user, request.getRejectionReason());

        log.info("Rejected service provider request for user: {}, reason: {}",
                user.getEmail(), request.getRejectionReason());

        sendRejectionEmail(user, request.getRejectionReason());

        return ApiResponse.success("Service provider request rejected and documents removed.");
    }

    private static ServiceProvider getProvider(User user) {
        ServiceProvider provider = user.getServiceProvider();

        if (provider == null) {
            throw new BadRequestException("User has not applied to become a service provider.");
        }

        if (provider.getApplicationStatus() == ApplicationStatus.REJECTED) {
            throw new BadRequestException("User's request is already rejected.");
        }

        if (provider.getApplicationStatus() == ApplicationStatus.APPROVED) {
            throw new BadRequestException("Cannot reject an already approved service provider.");
        }
        return provider;
    }

    @Override
    public ApiResponse<Page<ServiceProviderResponseDTO>> getPendingProviderApplications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("applicationDate").descending());

        Page<ServiceProvider> pendingProviders = serviceProviderRepository
                .findAllByApplicationStatus(ApplicationStatus.PENDING, pageable);

        Page<ServiceProviderResponseDTO> responsePage = pendingProviders.map(provider -> {
            ServiceProviderResponseDTO dto = modelMapper.map(provider, ServiceProviderResponseDTO.class);
            dto.setUserId(provider.getUser().getId());
            return dto;
        });
        return ApiResponse.success(responsePage, "Pending applications fetched successfully.");
    }

    @Override
    public ApiResponse<List<ServiceProviderResponseDTO>> getAllApprovedServiceProviders() {
        List<User> approvedUsers = userRepository.findAllServiceProviders().stream()
                .filter(user -> user.getServiceProvider() != null && user.getServiceProvider().getApplicationStatus().equals(ApplicationStatus.APPROVED))
                .toList();

        List<ServiceProviderResponseDTO> response = approvedUsers.stream()
                .map(user -> {
                    ServiceProvider provider = user.getServiceProvider();
                    ServiceProviderResponseDTO dto = modelMapper.map(provider, ServiceProviderResponseDTO.class);
                    dto.setUserId(provider.getUser().getId());
                    return dto;
                })
                .toList();

        return ApiResponse.success(response, "Approved service providers fetched successfully.");
    }

    private void sendApprovalEmail(User user) {
        notificationService.sendEmail(
                user.getEmail(),
                "Your Service Provider Application is Approved!",
                "approval-email.html",
                Map.of(
                        "name", user.getName(),
                        "dashboardUrl", "http://localhost:8085/provider/dashboard"
                )
        );

        // Save notification
        notificationService.saveNotification(
                user,
                "Your service provider application has been approved.",
                NotificationType.APPLICATION_APPROVED
        );
    }

    private void sendRejectionEmail(User user, String rejectionReason) {
        notificationService.sendEmail(
                user.getEmail(),
                "Your Service Provider Application is Rejected",
                "rejection-email.html",
                Map.of(
                        "name", user.getName(),
                        "rejectionReason", rejectionReason
                )
        );

        // Save notification
        notificationService.saveNotification(
                user,
                "Your service provider application has been rejected. Reason: " + rejectionReason,
                NotificationType.APPLICATION_REJECTED
        );
    }

    private void deleteProviderUploadedDocuments(ServiceProvider provider, User user, String rejectionReason) {
        try {
            String profilePicturePublicId = provider.getProfilePicturePublicId();
            String idProofPublicId = provider.getIdProofDocumentPublicId();
            String licenseDocPublicId = provider.getLicenseDocumentPublicId();

            if (profilePicturePublicId != null) {
                cloudinaryUtil.deleteFile(profilePicturePublicId, "image");
            }
            if (idProofPublicId != null) {
                cloudinaryUtil.deleteFile(idProofPublicId, "pdf");
            }
            if (licenseDocPublicId != null) {
                cloudinaryUtil.deleteFile(licenseDocPublicId, "pdf");
            }

            provider.setIdProofDocumentPublicId(null);
            provider.setLicenseDocumentPublicId(null);
            provider.setLicenseDocumentPublicId(null);
            provider.setProfilePictureUrl(null);
            provider.setIdProofDocumentUrl(null);
            provider.setLicenseDocumentUrl(null);

            provider.setApplicationStatus(ApplicationStatus.REJECTED);
            provider.setRejectedAt(LocalDateTime.now());

//            provider.setRejectionReason("Document quality is insufficient. Please re-upload clear images and pdfs.");
            provider.setRejectionReason(rejectionReason);
            serviceProviderRepository.save(provider);

        } catch (IOException e) {
            log.error("Failed to delete Cloudinary files for user {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
