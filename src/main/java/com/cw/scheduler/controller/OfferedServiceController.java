package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.CreateOfferingServiceRequestDTO;
import com.cw.scheduler.dto.response.OfferingServiceResponseDTO;
import com.cw.scheduler.service.interfaces.OfferingService;
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

    @PostMapping
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<ApiResponse<OfferingServiceResponseDTO>> createService(@Valid @RequestBody CreateOfferingServiceRequestDTO request) {
        ApiResponse<OfferingServiceResponseDTO> response = offeringService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<ApiResponse<List<OfferingServiceResponseDTO>>> getServicesByProvider() {
        return ResponseEntity.ok(offeringService.getServicesByProvider());
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<OfferingServiceResponseDTO>>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(offeringService.getAllServices(page, size));
    }
}
