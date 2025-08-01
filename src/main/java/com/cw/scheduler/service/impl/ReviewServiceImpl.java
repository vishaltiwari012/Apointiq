package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.ReviewRequestDTO;
import com.cw.scheduler.dto.response.ReviewResponseDTO;
import com.cw.scheduler.entity.OfferedService;
import com.cw.scheduler.entity.Review;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.exception.ResourceNotFoundException;
import com.cw.scheduler.repository.OfferedServiceRepository;
import com.cw.scheduler.repository.ReviewRepository;
import com.cw.scheduler.security.AuthenticationFacade;
import com.cw.scheduler.service.interfaces.ReviewService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AuthenticationFacade authenticationFacade;
    private final OfferedServiceRepository offeredServiceRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<ReviewResponseDTO> createReview(ReviewRequestDTO request) {
        User currentUser = authenticationFacade.getCurrentUser();
        OfferedService offeredService = offeredServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        Review review = new Review();
        review.setComment(request.getComment());
        review.setRating(request.getRating());
        review.setUser(currentUser);
        review.setService(offeredService);
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        ReviewResponseDTO response = new ReviewResponseDTO(
                savedReview.getId(),
                review.getUser().getName(),
                review.getService().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
        return ApiResponse.success(response, "Review created successfully.");
    }

    @Override
    public ApiResponse<List<ReviewResponseDTO>> getReviewsByService(Long serviceId) {
        OfferedService offeredService = offeredServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        List<ReviewResponseDTO> response = reviewRepository.findByServiceId(serviceId)
                .stream().map(
                        review -> modelMapper.map(review, ReviewResponseDTO.class)
                ).toList();

        return ApiResponse.success(response, "All reviews of service " + offeredService.getName());
    }

    @Override
    public ApiResponse<List<ReviewResponseDTO>> getReviewsByUser() {
        User currentUser = authenticationFacade.getCurrentUser();
        List<ReviewResponseDTO> response = reviewRepository.findByUserId(currentUser.getId())
                .stream().map(
                        review -> modelMapper.map(review, ReviewResponseDTO.class)
                ).toList();
        return ApiResponse.success(response, "All reviews of user " + currentUser.getName());
    }
}
