package com.skillsaathi.service.impl;

import com.skillsaathi.dto.review.ReviewResponse;
import com.skillsaathi.dto.review.SubmitReviewRequest;
import com.skillsaathi.entity.Connection;
import com.skillsaathi.entity.Review;
import com.skillsaathi.entity.User;
import com.skillsaathi.entity.enums.ConnectionStatus;
import com.skillsaathi.entity.enums.NotificationType;
import com.skillsaathi.exception.BadRequestException;
import com.skillsaathi.exception.ResourceNotFoundException;
import com.skillsaathi.repository.ConnectionRepository;
import com.skillsaathi.repository.ReviewRepository;
import com.skillsaathi.repository.UserRepository;
import com.skillsaathi.service.NotificationService;
import com.skillsaathi.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public ReviewResponse submitReview(Long reviewerId, Long connectionId, SubmitReviewRequest request) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));

        boolean isRequester = connection.getRequester().getId().equals(reviewerId);
        boolean isReceiver = connection.getReceiver().getId().equals(reviewerId);
        if (!isRequester && !isReceiver) {
            throw new BadRequestException("You can only review connections you were part of");
        }
        if (connection.getStatus() != ConnectionStatus.ACCEPTED) {
            throw new BadRequestException("You can only review completed/accepted connections");
        }

        User reviewer = isRequester ? connection.getRequester() : connection.getReceiver();
        User reviewee = isRequester ? connection.getReceiver() : connection.getRequester();

        // DB also enforces this via uq_review_once, but checking here gives a clean 400
        // instead of surfacing a raw constraint-violation exception to the client.
        boolean alreadyReviewed = reviewRepository.findByRevieweeId(reviewee.getId()).stream()
                .anyMatch(r -> r.getConnection().getId().equals(connectionId) && r.getReviewer().getId().equals(reviewerId));
        if (alreadyReviewed) {
            throw new BadRequestException("You've already reviewed this connection");
        }

        Review review = Review.builder()
                .connection(connection)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);
        recalculateAverageRating(reviewee.getId());

        notificationService.create(reviewee.getId(), NotificationType.REVIEW_RECEIVED,
                "New review received", reviewer.getName() + " left you a " + request.getRating() + "-star review",
                review.getId());

        return toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForUser(Long userId) {
        return reviewRepository.findByRevieweeId(userId).stream().map(this::toResponse).toList();
    }

    private void recalculateAverageRating(Long revieweeId) {
        Double avg = reviewRepository.findAverageRatingForUser(revieweeId);
        User user = userRepository.findById(revieweeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BigDecimal rounded = avg != null
                ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        user.setAverageRating(rounded);
        userRepository.save(user);
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .connectionId(r.getConnection().getId())
                .reviewerId(r.getReviewer().getId())
                .reviewerName(r.getReviewer().getName())
                .reviewerProfilePicture(r.getReviewer().getProfilePictureUrl())
                .revieweeId(r.getReviewee().getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
