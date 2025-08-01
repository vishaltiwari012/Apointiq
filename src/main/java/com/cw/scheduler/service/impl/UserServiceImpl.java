package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ServiceProviderRequestDTO;
import com.cw.scheduler.dto.request.UserUpdateRequestDTO;
import com.cw.scheduler.dto.response.ServiceProviderResponseDTO;
import com.cw.scheduler.dto.response.UserProfileResponseDTO;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.repository.UserRepository;
import com.cw.scheduler.security.AuthenticationFacade;
import com.cw.scheduler.service.interfaces.ServiceProviderService;
import com.cw.scheduler.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AuthenticationFacade authenticationFacade;
    private final ServiceProviderService serviceProviderService;


    @Override
    public ApiResponse<UserProfileResponseDTO> getMyProfile() {
        User user = authenticationFacade.getCurrentUser();
        return ApiResponse.success(modelMapper.map(user, UserProfileResponseDTO.class), "User Profile fetched successfully.");
    }

    @Override
    public ApiResponse<UserProfileResponseDTO> updateMyProfile(UserUpdateRequestDTO request) {
        User user = authenticationFacade.getCurrentUser();

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User savedUser = userRepository.save(user);

        return ApiResponse.success(modelMapper.map(savedUser, UserProfileResponseDTO.class), "User Profile updated successfully.");
    }

    @Override
    public ApiResponse<String> applyForServiceProvider(ServiceProviderRequestDTO request){
        User user = authenticationFacade.getCurrentUser();
        return serviceProviderService.apply(request, user);
    }

    @Override
    public ApiResponse<ServiceProviderResponseDTO> getProviderApplicationForCurrentUser() {
        User user = authenticationFacade.getCurrentUser();
        return serviceProviderService.getApplicationByUser(user);
    }

    @Override
    public ApiResponse<String> uploadProviderDocuments(MultipartFile profilePicture, MultipartFile idProof, MultipartFile licenseDoc) throws FileUploadException {
        User currentUser = authenticationFacade.getCurrentUser();
        return serviceProviderService.uploadDocuments(currentUser, profilePicture, idProof, licenseDoc);
    }

    @Override
    public ApiResponse<String> getProviderApplicationStatus() {
        User user = authenticationFacade.getCurrentUser();
        return serviceProviderService.getApplicationStatus(user);
    }

}
