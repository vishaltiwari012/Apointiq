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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AuthenticationFacade authenticationFacade;
    private final OfferedServiceRepository offeredServiceRepository;
    private final ModelMapper modelMapper;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "serviceReviews", key = "#request.serviceId"),
            @CacheEvict(value = "userReviews", key = "@authenticationFacade.getCurrentUserId()")
    })
    public ApiResponse<ReviewResponseDTO> createReview(ReviewRequestDTO request) {
        User currentUser = authenticationFacade.getCurrentUser();
        log.info("UserId={} creating review for serviceId={}", currentUser.getId(), request.getServiceId());

        OfferedService offeredService = offeredServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        Review review = new Review();
        review.setComment(request.getComment());
        review.setRating(request.getRating());
        review.setUser(currentUser);
        review.setService(offeredService);
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        log.debug("Review saved: reviewId={}, serviceId={}, userId={}",
                savedReview.getId(), offeredService.getId(), currentUser.getId());

        ReviewResponseDTO response = modelMapper.map(savedReview, ReviewResponseDTO.class);
        return ApiResponse.success(response, "Review created successfully.");
    }

    @Override
    @Cacheable(value = "serviceReviews", key = "#serviceId")
    public ApiResponse<List<ReviewResponseDTO>> getReviewsByService(Long serviceId) {
        log.info("Fetching reviews for serviceId={}", serviceId);

        OfferedService offeredService = offeredServiceRepository.findById(serviceId)
                .orElseThrow(() -> {
                    log.warn("Service not found for serviceId={}", serviceId);
                    return new ResourceNotFoundException("Service not found");
                });

        List<ReviewResponseDTO> response = reviewRepository.findByServiceId(serviceId)
                .stream()
                .map(review -> modelMapper.map(review, ReviewResponseDTO.class))
                .toList();

        log.debug("Found {} reviews for serviceId={}", response.size(), serviceId);
        return ApiResponse.success(response, "All reviews of service " + offeredService.getName());
    }

    @Override
    @Cacheable(value = "userReviews", key = "@authenticationFacade.getCurrentUserId()")
    public ApiResponse<List<ReviewResponseDTO>> getReviewsByUser() {
        User currentUser = authenticationFacade.getCurrentUser();
        log.info("Fetching reviews for userId={}", currentUser.getId());

        List<ReviewResponseDTO> response = reviewRepository.findByUserId(currentUser.getId())
                .stream()
                .map(review -> modelMapper.map(review, ReviewResponseDTO.class))
                .toList();

        log.debug("Found {} reviews for userId={}", response.size(), currentUser.getId());
        return ApiResponse.success(response, "All reviews of user " + currentUser.getName());
    }
}
