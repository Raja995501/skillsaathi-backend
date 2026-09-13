package com.skillsaathi.service;

import com.skillsaathi.dto.review.ReviewResponse;
import com.skillsaathi.dto.review.SubmitReviewRequest;

import java.util.List;

public interface ReviewService {
    ReviewResponse submitReview(Long reviewerId, Long connectionId, SubmitReviewRequest request);
    List<ReviewResponse> getReviewsForUser(Long userId);
}
