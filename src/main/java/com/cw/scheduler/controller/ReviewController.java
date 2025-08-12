package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ReviewRequestDTO;
import com.cw.scheduler.dto.response.ReviewResponseDTO;
import com.cw.scheduler.ratelimit.RateLimit;
import com.cw.scheduler.service.interfaces.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Review APIs")
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class ReviewController {

    private final ReviewService reviewService;

    @RateLimit(capacity = 5, refillTokens = 1, refillDurationSeconds = 60)
    @Operation(summary = "Create a review", description = "Submits a review for a completed service.")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> createReview(@Valid @RequestBody ReviewRequestDTO dto) {
        return new ResponseEntity<>(reviewService.createReview(dto), HttpStatus.CREATED);
    }

    @RateLimit(capacity = 10, refillTokens = 2, refillDurationSeconds = 60)
    @Operation(summary = "Get reviews for a service", description = "Retrieves all reviews for a specific service by service ID.")
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(reviewService.getReviewsByService(serviceId));
    }

    @RateLimit(capacity = 10, refillTokens = 2, refillDurationSeconds = 60)
    @Operation(summary = "Get my reviews", description = "Retrieves all reviews submitted by the logged-in user.")
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByUser() {
        return ResponseEntity.ok(reviewService.getReviewsByUser());
    }
}

