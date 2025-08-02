package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ServiceProviderRequestDTO;
import com.cw.scheduler.dto.request.UserUpdateRequestDTO;
import com.cw.scheduler.dto.response.ServiceProviderResponseDTO;
import com.cw.scheduler.dto.response.UserProfileResponseDTO;
import com.cw.scheduler.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User APIs")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PostMapping("/test-upload")
    public String testUpload(@RequestParam MultipartFile file) {
        return "received: " + file.getOriginalFilename();
    }

    @PutMapping("/update/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> updateProfile(@RequestBody UserUpdateRequestDTO requestDTO) {
        return ResponseEntity.ok(userService.updateMyProfile(requestDTO));
    }

    @PostMapping("/apply-provider")
    public ResponseEntity<ApiResponse<String>> applyForServiceProviderWithBasicInfo(@RequestBody @Valid ServiceProviderRequestDTO dto) {
        return ResponseEntity.ok(userService.applyForServiceProvider(dto));
    }

    @GetMapping("/provider/application")
    public ResponseEntity<ApiResponse<ServiceProviderResponseDTO>> getProviderApplication() {
        return ResponseEntity.ok(userService.getProviderApplicationForCurrentUser());
    }

    @PostMapping("/upload-documents")
    public ResponseEntity<ApiResponse<String>> uploadDocuments(
            @RequestParam MultipartFile profilePicture,
            @RequestParam MultipartFile idProof,
            @RequestParam MultipartFile licenseDoc) throws FileUploadException {
        log.info(profilePicture.getOriginalFilename());
        return ResponseEntity.ok(userService.uploadProviderDocuments(profilePicture, idProof, licenseDoc));
    }

    @GetMapping("/provider-status")
    public ResponseEntity<ApiResponse<String>> getProviderApplicationStatus() {
        return ResponseEntity.ok(userService.getProviderApplicationStatus());
    }
}
