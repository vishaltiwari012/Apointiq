package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ServiceProviderRequestDTO;
import com.cw.scheduler.dto.response.ServiceProviderResponseDTO;
import com.cw.scheduler.entity.User;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

public interface ServiceProviderService {
    // Handles application to become a service provider
    ApiResponse<String> apply(ServiceProviderRequestDTO request, User user);

    // Get current user's provider profile
    ApiResponse<ServiceProviderResponseDTO> getMyProviderProfile();

    ApiResponse<ServiceProviderResponseDTO> getApplicationByUser(User user);

    ApiResponse<String> uploadDocuments(User user, MultipartFile profilePicture, MultipartFile idProof, MultipartFile licenseDoc) throws FileUploadException;
    // Get Application Status
    ApiResponse<String> getApplicationStatus(User user);

}
