package com.ttt.cinevibe.controller;

import com.ttt.cinevibe.dto.request.MovieReviewRequest;
import com.ttt.cinevibe.dto.response.MovieReviewResponse;
import com.ttt.cinevibe.service.MovieReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Movie Review API", description = "Endpoints to manage movie reviews and ratings")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class MovieReviewController {

    private final MovieReviewService reviewService;

    @Operation(summary = "Get user's reviews", description = "Returns all reviews created by the authenticated user")
    @GetMapping("/my")
    public ResponseEntity<Page<MovieReviewResponse>> getUserReviews(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(reviewService.getUserReviews(userUid, pageable));
    }

    @Operation(summary = "Get reviews for a movie", description = "Returns all reviews for a specific movie")
    @GetMapping("/movie/{tmdbMovieId}")
    public ResponseEntity<Page<MovieReviewResponse>> getMovieReviews(
            @PathVariable Long tmdbMovieId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getMovieReviews(tmdbMovieId, pageable));
    }

    @Operation(summary = "Get a review by ID", description = "Returns a specific review by ID")
    @GetMapping("/{reviewId}")
    public ResponseEntity<MovieReviewResponse> getReviewById(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewById(reviewId));
    }

    @Operation(summary = "Create a new review", description = "Creates a new review for a movie")
    @PostMapping
    public ResponseEntity<MovieReviewResponse> createReview(
            @Valid @RequestBody MovieReviewRequest request,
            Authentication authentication) {
        String userUid = authentication.getName();
        MovieReviewResponse created = reviewService.createReview(userUid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a review", description = "Updates an existing review owned by the authenticated user")
    @PutMapping("/{reviewId}")
    public ResponseEntity<MovieReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody MovieReviewRequest request,
            Authentication authentication) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(reviewService.updateReview(userUid, reviewId, request));
    }

    @Operation(summary = "Delete a review", description = "Deletes a review owned by the authenticated user")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        String userUid = authentication.getName();
        reviewService.deleteReview(userUid, reviewId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Like a review", description = "Adds a like to a review")
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<MovieReviewResponse> likeReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(reviewService.likeReview(userUid, reviewId));
    }

    @Operation(summary = "Unlike a review", description = "Removes a like from a review")
    @DeleteMapping("/{reviewId}/like")
    public ResponseEntity<MovieReviewResponse> unlikeReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(reviewService.unlikeReview(userUid, reviewId));
    }

    @Operation(summary = "Check if user has reviewed movie", description = "Verifies if the user has already reviewed a specific movie")
    @GetMapping("/check/{tmdbMovieId}")
    public ResponseEntity<Map<String, Boolean>> hasUserReviewedMovie(
            @PathVariable Long tmdbMovieId,
            Authentication authentication) {
        String userUid = authentication.getName();
        boolean hasReviewed = reviewService.hasUserReviewedMovie(userUid, tmdbMovieId);
        return ResponseEntity.ok(Map.of("hasReviewed", hasReviewed));
    }

    @Operation(summary = "Get user's review for a movie", description = "Returns the review created by the authenticated user for a specific movie")
    @GetMapping("/user/movie/{tmdbMovieId}")
    public ResponseEntity<MovieReviewResponse> getUserReviewForMovie(
            @PathVariable Long tmdbMovieId,
            Authentication authentication) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(reviewService.getUserReviewForMovie(userUid, tmdbMovieId));
    }
}