package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ServiceProviderRequestDTO;
import com.cw.scheduler.dto.request.UserUpdateRequestDTO;
import com.cw.scheduler.dto.response.ServiceProviderResponseDTO;
import com.cw.scheduler.dto.response.UserProfileResponseDTO;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    ApiResponse<UserProfileResponseDTO> getMyProfile();
    ApiResponse<UserProfileResponseDTO> updateMyProfile(UserUpdateRequestDTO request);
    ApiResponse<String> applyForServiceProvider(ServiceProviderRequestDTO request);
    ApiResponse<ServiceProviderResponseDTO> getProviderApplicationForCurrentUser();
    ApiResponse<String> uploadProviderDocuments(MultipartFile profilePicture, MultipartFile idProof, MultipartFile licenseDoc) throws FileUploadException;
    ApiResponse<String> getProviderApplicationStatus();
}
