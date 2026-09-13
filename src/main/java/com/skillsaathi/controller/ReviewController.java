package com.skillsaathi.controller;

import com.skillsaathi.dto.common.ApiResponse;
import com.skillsaathi.dto.review.ReviewResponse;
import com.skillsaathi.dto.review.SubmitReviewRequest;
import com.skillsaathi.service.ReviewService;
import com.skillsaathi.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/connections/{id}/review")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @PathVariable Long id, @Valid @RequestBody SubmitReviewRequest request) {
        ReviewResponse response = reviewService.submitReview(SecurityUtils.getCurrentUserId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Review submitted", response));
    }

    @GetMapping("/users/{id}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewsForUser(id)));
    }
}
