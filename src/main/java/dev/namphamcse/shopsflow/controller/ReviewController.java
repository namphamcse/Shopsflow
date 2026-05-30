package dev.namphamcse.shopsflow.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.namphamcse.shopsflow.dto.request.ReviewRequest;
import dev.namphamcse.shopsflow.dto.response.ReviewResponse;
import dev.namphamcse.shopsflow.entity.User;
import dev.namphamcse.shopsflow.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest req) {
        ReviewResponse created = reviewService.createReview(user, productId, req);
        return ResponseEntity
                .created(URI.create("/api/reviews/" + created.getId()))
                .body(created);
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> editReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest req) {
        return ResponseEntity.ok(reviewService.editReview(user, reviewId, req));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId) {
        reviewService.deleteReview(user, reviewId);
        return ResponseEntity.noContent().build();
    }
}
