package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.CreateIndividualServiceRequestDTO;
import com.cw.scheduler.dto.response.IndividualServiceResponseDTO;
import com.cw.scheduler.service.interfaces.IndividualServiceManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/individual-services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SERVICE_PROVIDER')")
public class IndividualServiceController {

    private final IndividualServiceManager individualServiceManager;

    @PostMapping("/{offeredServiceId}")
    public ResponseEntity<ApiResponse<IndividualServiceResponseDTO>> create(
            @PathVariable Long offeredServiceId,
            @Validated @RequestBody CreateIndividualServiceRequestDTO request) {
        return ResponseEntity.ok(individualServiceManager.createIndividualService(offeredServiceId, request));
    }

    @GetMapping("/{offeredServiceId}")
    public ResponseEntity<ApiResponse<List<IndividualServiceResponseDTO>>> getAll(
            @PathVariable Long offeredServiceId) {
        return ResponseEntity.ok(individualServiceManager.getIndividualServicesByOfferedService(offeredServiceId));
    }
}
