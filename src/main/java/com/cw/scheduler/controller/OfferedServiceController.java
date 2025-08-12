package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.CreateOfferingServiceRequestDTO;
import com.cw.scheduler.dto.response.OfferingServiceResponseDTO;
import com.cw.scheduler.ratelimit.RateLimit;
import com.cw.scheduler.service.interfaces.OfferingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Offered Service APIs")
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class OfferedServiceController {

    private final OfferingService offeringService;

    @Operation(summary = "Create a new offered service for the provider", description = "Allows a service provider to create and register a new offered service with its details.")
    @RateLimit(capacity = 5, refillTokens = 1, refillDurationSeconds = 60)
    @PostMapping
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<ApiResponse<OfferingServiceResponseDTO>> createService(
            @Valid @RequestBody CreateOfferingServiceRequestDTO request) {
        ApiResponse<OfferingServiceResponseDTO> response = offeringService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Retrieve all services created by the logged-in provider", description = "Fetches a list of services for the current service provider.")
    @RateLimit(capacity = 10, refillTokens = 2, refillDurationSeconds = 60)
    @GetMapping("/my")
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<ApiResponse<List<OfferingServiceResponseDTO>>> getServicesByProvider() {
        return ResponseEntity.ok(offeringService.getServicesByProvider());
    }

    @Operation(summary = "Retrieve all available offered services", description = "Provides a paginated list of all offered services accessible to customers.")
    @RateLimit(capacity = 20, refillTokens = 5, refillDurationSeconds = 60)
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<OfferingServiceResponseDTO>>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(offeringService.getAllServices(page, size));
    }
}
