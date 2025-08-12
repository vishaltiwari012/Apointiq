package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ProviderRejectionRequestDTO;
import com.cw.scheduler.dto.response.ServiceProviderResponseDTO;
import com.cw.scheduler.ratelimit.RateLimit;
import com.cw.scheduler.service.interfaces.AdminProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin-Provider APIs")
@RestController
@RequestMapping("/admin/providers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProviderController {

    private final AdminProviderService adminProviderService;

    @RateLimit(capacity = 10, refillTokens = 2, refillDurationSeconds = 60)
    @Operation(summary = "Get pending provider applications", description = "Retrieves a paginated list of service provider applications that are pending approval.")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ServiceProviderResponseDTO>>> getPendingApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminProviderService.getPendingProviderApplications(page, size));
    }

    @RateLimit(capacity = 5, refillTokens = 1, refillDurationSeconds = 60)
    @Operation(summary = "Approve a provider application", description = "Approves the service provider application for a specified user ID.")
    @PostMapping("/{userId}/approve")
    public ResponseEntity<ApiResponse<String>> approveProvider(@PathVariable Long userId) {
        return ResponseEntity.ok(adminProviderService.approveProviderRequest(userId));
    }

    @RateLimit(capacity = 5, refillTokens = 1, refillDurationSeconds = 60)
    @Operation(summary = "Reject a provider application", description = "Rejects a service provider application with a given rejection reason.")
    @PostMapping("/application/reject")
    public ResponseEntity<ApiResponse<String>> rejectProvider(@RequestBody ProviderRejectionRequestDTO request) {
        return ResponseEntity.ok(adminProviderService.rejectProviderRequest(request));
    }

    @RateLimit(capacity = 15, refillTokens = 3, refillDurationSeconds = 60)
    @Operation(summary = "Get approved service providers", description = "Retrieves the list of all approved service providers.")
    @GetMapping("/approved")
    public ResponseEntity<ApiResponse<List<ServiceProviderResponseDTO>>> getApprovedProviders() {
        return ResponseEntity.ok(adminProviderService.getAllApprovedServiceProviders());
    }
}
