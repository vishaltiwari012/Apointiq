package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ReviewRequestDTO;
import com.cw.scheduler.dto.response.ReviewResponseDTO;

import java.util.List;

public interface ReviewService {
    ApiResponse<ReviewResponseDTO> createReview(ReviewRequestDTO request);
    ApiResponse<List<ReviewResponseDTO>> getReviewsByService(Long serviceId);
    ApiResponse<List<ReviewResponseDTO>> getReviewsByUser();
}
