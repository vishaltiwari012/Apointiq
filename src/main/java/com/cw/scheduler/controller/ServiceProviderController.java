package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.response.ServiceProviderResponseDTO;
import com.cw.scheduler.ratelimit.RateLimit;
import com.cw.scheduler.service.interfaces.ServiceProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Service Provider APIs")
@RestController
@RequestMapping("/providers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SERVICE_PROVIDER')")
public class ServiceProviderController {

    private final ServiceProviderService serviceProviderService;

    @RateLimit(capacity = 10, refillTokens = 2, refillDurationSeconds = 60)
    @Operation(summary = "Get my provider profile", description = "Retrieves the logged-in service provider's profile details.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ServiceProviderResponseDTO>> getMyProviderProfile() {
        ApiResponse<ServiceProviderResponseDTO> response = serviceProviderService.getMyProviderProfile();
        return ResponseEntity.ok(response);
    }
}

