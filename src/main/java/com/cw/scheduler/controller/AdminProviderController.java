package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ProviderRejectionRequestDTO;
import com.cw.scheduler.dto.response.ServiceProviderResponseDTO;
import com.cw.scheduler.service.interfaces.AdminProviderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<ServiceProviderResponseDTO>>> getPendingApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminProviderService.getPendingProviderApplications(page, size));
    }

    @PostMapping("/{userId}/approve")
    public ResponseEntity<ApiResponse<String>> approveProvider(@PathVariable Long userId) {
        return ResponseEntity.ok(adminProviderService.approveProviderRequest(userId));
    }

    @PostMapping("/application/reject")
    public ResponseEntity<ApiResponse<String>> rejectProvider(@RequestBody ProviderRejectionRequestDTO request) {
        return ResponseEntity.ok(adminProviderService.rejectProviderRequest(request));
    }

    @GetMapping("/approved")
    public ResponseEntity<ApiResponse<List<ServiceProviderResponseDTO>>> getApprovedProviders() {
        return ResponseEntity.ok(adminProviderService.getAllApprovedServiceProviders());
    }

}
