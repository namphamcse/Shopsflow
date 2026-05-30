package dev.namphamcse.shopsflow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.namphamcse.shopsflow.dto.request.ReviewRequest;
import dev.namphamcse.shopsflow.dto.response.ReviewResponse;
import dev.namphamcse.shopsflow.entity.Product;
import dev.namphamcse.shopsflow.entity.Review;
import dev.namphamcse.shopsflow.entity.User;
import dev.namphamcse.shopsflow.exception.BusinessRuleViolationException;
import dev.namphamcse.shopsflow.exception.ResourceNotFoundException;
import dev.namphamcse.shopsflow.mapper.ReviewMapper;
import dev.namphamcse.shopsflow.repository.ProductRepository;
import dev.namphamcse.shopsflow.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepo;
    private final ProductRepository productRepo;

    @Transactional
    public ReviewResponse createReview(User user, Long productId, ReviewRequest req) {
        if (reviewRepo.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new BusinessRuleViolationException("You already reviewed this product.");
        }
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        Review review = new Review(user, product, req.getStars(), req.getComment());
        Review saved = reviewRepo.save(review);
        return ReviewMapper.toResponse(saved);
    }

    @Transactional
    public ReviewResponse editReview(User user, Long reviewId, ReviewRequest req) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new BusinessRuleViolationException("You can only edit your own review.");
        }

        review.setStars(req.getStars());
        review.setComment(req.getComment());

        return ReviewMapper.toResponse(review);
    }

    @Transactional
    public void deleteReview(User user, Long reviewId) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new BusinessRuleViolationException("You can only delete your own review.");
        }

        reviewRepo.delete(review);
    }

    public List<ReviewResponse> getProductReviews(Long productId) {
        if (!productRepo.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        return reviewRepo.findByProductId(productId).stream()
                .map(ReviewMapper::toResponse)
                .toList();
    }
}
