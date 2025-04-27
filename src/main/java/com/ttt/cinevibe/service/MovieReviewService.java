package com.ttt.cinevibe.service;

import com.ttt.cinevibe.dto.request.MovieReviewRequest;
import com.ttt.cinevibe.dto.response.MovieReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovieReviewService {
    
    Page<MovieReviewResponse> getUserReviews(String userUid, Pageable pageable);
    
    Page<MovieReviewResponse> getMovieReviews(Long tmdbMovieId, Pageable pageable);
    
    MovieReviewResponse getReviewById(Long reviewId);
    
    MovieReviewResponse createReview(String userUid, MovieReviewRequest request);
    
    MovieReviewResponse updateReview(String userUid, Long reviewId, MovieReviewRequest request);
    
    void deleteReview(String userUid, Long reviewId);
    
    MovieReviewResponse likeReview(String userUid, Long reviewId);
    
    MovieReviewResponse unlikeReview(String userUid, Long reviewId);
    
    boolean hasUserReviewedMovie(String userUid, Long tmdbMovieId);
}