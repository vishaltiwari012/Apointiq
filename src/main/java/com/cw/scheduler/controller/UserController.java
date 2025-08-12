package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ServiceProviderRequestDTO;
import com.cw.scheduler.dto.request.UserUpdateRequestDTO;
import com.cw.scheduler.dto.response.ServiceProviderResponseDTO;
import com.cw.scheduler.dto.response.UserProfileResponseDTO;
import com.cw.scheduler.ratelimit.RateLimit;
import com.cw.scheduler.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Get current user profile", description = "Fetch the profile details of the currently logged-in user.")
    @RateLimit(capacity = 15, refillTokens = 5, refillDurationSeconds = 60)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @Operation(summary = "Test file upload", description = "Uploads a file for testing purposes. Not for production use.")
    @RateLimit(capacity = 5, refillTokens = 2, refillDurationSeconds = 60)
    @PostMapping("/test-upload")
    public String testUpload(@RequestParam MultipartFile file) {
        return "received: " + file.getOriginalFilename();
    }

    @Operation(summary = "Update user profile", description = "Update personal details of the currently logged-in user.")
    @RateLimit(capacity = 5, refillTokens = 2, refillDurationSeconds = 60)
    @PutMapping("/update/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> updateProfile(@RequestBody UserUpdateRequestDTO requestDTO) {
        return ResponseEntity.ok(userService.updateMyProfile(requestDTO));
    }

    @Operation(summary = "Apply as a service provider", description = "Submit basic information to apply for a service provider account.")
    @RateLimit(capacity = 3, refillTokens = 1, refillDurationSeconds = 60)
    @PostMapping("/apply-provider")
    public ResponseEntity<ApiResponse<String>> applyForServiceProviderWithBasicInfo(@RequestBody @Valid ServiceProviderRequestDTO dto) {
        return ResponseEntity.ok(userService.applyForServiceProvider(dto));
    }

    @Operation(summary = "Get provider application details", description = "Retrieve the service provider application submitted by the current user.")
    @RateLimit(capacity = 5, refillTokens = 2, refillDurationSeconds = 60)
    @GetMapping("/provider/application")
    public ResponseEntity<ApiResponse<ServiceProviderResponseDTO>> getProviderApplication() {
        return ResponseEntity.ok(userService.getProviderApplicationForCurrentUser());
    }

    @Operation(summary = "Upload provider documents", description = "Upload required documents such as profile picture, ID proof, and license for service provider application.")
    @RateLimit(capacity = 3, refillTokens = 1, refillDurationSeconds = 60)
    @PostMapping("/upload-documents")
    public ResponseEntity<ApiResponse<String>> uploadDocuments(
            @RequestParam MultipartFile profilePicture,
            @RequestParam MultipartFile idProof,
            @RequestParam MultipartFile licenseDoc) throws FileUploadException {
        log.info(profilePicture.getOriginalFilename());
        return ResponseEntity.ok(userService.uploadProviderDocuments(profilePicture, idProof, licenseDoc));
    }

    @Operation(summary = "Get provider application status", description = "Check the current approval status of the service provider application.")
    @RateLimit(capacity = 5, refillTokens = 2, refillDurationSeconds = 60)
    @GetMapping("/provider-status")
    public ResponseEntity<ApiResponse<String>> getProviderApplicationStatus() {
        return ResponseEntity.ok(userService.getProviderApplicationStatus());
    }
}
