package com.ttt.cinevibe.service.impl;

import com.ttt.cinevibe.dto.request.MovieReviewRequest;
import com.ttt.cinevibe.dto.response.MovieReviewResponse;
import com.ttt.cinevibe.dto.response.UserResponse;
import com.ttt.cinevibe.exception.DuplicateResourceException;
import com.ttt.cinevibe.exception.ResourceNotFoundException;
import com.ttt.cinevibe.exception.UnauthorizedException;
import com.ttt.cinevibe.model.MovieReview;
import com.ttt.cinevibe.model.User;
import com.ttt.cinevibe.repository.MovieReviewRepository;
import com.ttt.cinevibe.repository.UserRepository;
import com.ttt.cinevibe.service.CommentService;
import com.ttt.cinevibe.service.MovieReviewService;
import com.ttt.cinevibe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieReviewServiceImpl implements MovieReviewService {

    private final MovieReviewRepository reviewRepository;
    private final UserService userService;
    private final CommentService commentService;
    private final UserRepository userRepository;

    @Override
    public Page<MovieReviewResponse> getUserReviews(String userUid, Pageable pageable) {
        User user = getUserOrThrow(userUid);
        return reviewRepository.findByUser(user, pageable)
                .map(review -> mapToMovieReviewResponse(review, commentService.getCommentCountForReview(review.getId())));
    }

    @Override
    public Page<MovieReviewResponse> getMovieReviews(Long tmdbMovieId, Pageable pageable) {
        return reviewRepository.findByTmdbMovieId(tmdbMovieId, pageable)
                .map(review -> mapToMovieReviewResponse(review, commentService.getCommentCountForReview(review.getId())));
    }

    @Override
    public MovieReviewResponse getReviewById(Long reviewId) {
        MovieReview review = getReviewOrThrow(reviewId);
        int commentCount = commentService.getCommentCountForReview(reviewId);
        return mapToMovieReviewResponse(review, commentCount);
    }

    @Override
    @Transactional
    public MovieReviewResponse createReview(String userUid, MovieReviewRequest request) {
        User user = getUserOrThrow(userUid);
        
        // Check if the user has already reviewed this movie
        if (reviewRepository.existsByUserAndTmdbMovieId(user, request.getTmdbMovieId())) {
            throw new DuplicateResourceException("You have already reviewed this movie. Please update your existing review.");
        }
        
        MovieReview review = MovieReview.builder()
                .user(user)
                .tmdbMovieId(request.getTmdbMovieId())
                .movieTitle(request.getMovieTitle())
                .reviewText(request.getReviewText())
                .rating(request.getRating())
                .containsSpoilers(request.isContainsSpoilers())
                .likesCount(0)
                .build();
        
        review = reviewRepository.save(review);
        
        // Increment the user's review count
        Integer currentReviewCount = user.getReviewCount() != null ? user.getReviewCount() : 0;
        user.setReviewCount(currentReviewCount + 1);
        userRepository.save(user);
        
        log.info("Created new review with ID: {} for movie: {} by user: {}. Updated review count to: {}", 
                review.getId(), request.getTmdbMovieId(), userUid, user.getReviewCount());
        
        return mapToMovieReviewResponse(review, 0);
    }

    @Override
    @Transactional
    public MovieReviewResponse updateReview(String userUid, Long reviewId, MovieReviewRequest request) {
        MovieReview review = getReviewOrThrow(reviewId);
        
        // Verify the review belongs to the user
        if (!review.getUser().getFirebaseUid().equals(userUid)) {
            throw new UnauthorizedException("You don't have permission to update this review");
        }
        
        // Update review fields
        review.setReviewText(request.getReviewText());
        review.setRating(request.getRating());
        review.setContainsSpoilers(request.isContainsSpoilers());
        
        review = reviewRepository.save(review);
        log.info("Updated review with ID: {} by user: {}", reviewId, userUid);
        
        int commentCount = commentService.getCommentCountForReview(reviewId);
        return mapToMovieReviewResponse(review, commentCount);
    }

    @Override
    @Transactional
    public void deleteReview(String userUid, Long reviewId) {
        MovieReview review = getReviewOrThrow(reviewId);
        
        // Verify the review belongs to the user
        if (!review.getUser().getFirebaseUid().equals(userUid)) {
            throw new UnauthorizedException("You don't have permission to delete this review");
        }
        
        // Get the user and decrement the review count
        User user = review.getUser();
        Integer currentReviewCount = user.getReviewCount() != null ? user.getReviewCount() : 0;
        // Ensure the count doesn't go below 0
        if (currentReviewCount > 0) {
            user.setReviewCount(currentReviewCount - 1);
            userRepository.save(user);
        }
        
        reviewRepository.delete(review);
        log.info("Deleted review with ID: {} by user: {}. Updated review count to: {}", 
                reviewId, userUid, user.getReviewCount());
    }

    @Override
    @Transactional
    public MovieReviewResponse likeReview(String userUid, Long reviewId) {
        // In a real-world scenario, you'd track which users liked which reviews
        // Here we're just incrementing the counter for simplicity
        MovieReview review = getReviewOrThrow(reviewId);
        review.setLikesCount(review.getLikesCount() + 1);
        review = reviewRepository.save(review);
        
        log.info("User {} liked review {}", userUid, reviewId);
        
        int commentCount = commentService.getCommentCountForReview(reviewId);
        return mapToMovieReviewResponse(review, commentCount);
    }

    @Override
    @Transactional
    public MovieReviewResponse unlikeReview(String userUid, Long reviewId) {
        // In a real-world scenario, you'd track which users liked which reviews
        // Here we're just decrementing the counter for simplicity
        MovieReview review = getReviewOrThrow(reviewId);
        
        // Ensure likes count doesn't go below 0
        int currentLikes = review.getLikesCount();
        if (currentLikes > 0) {
            review.setLikesCount(currentLikes - 1);
            review = reviewRepository.save(review);
            log.info("User {} unliked review {}", userUid, reviewId);
        }
        
        int commentCount = commentService.getCommentCountForReview(reviewId);
        return mapToMovieReviewResponse(review, commentCount);
    }

    @Override
    public boolean hasUserReviewedMovie(String userUid, Long tmdbMovieId) {
        try {
            User user = getUserOrThrow(userUid);
            return reviewRepository.existsByUserAndTmdbMovieId(user, tmdbMovieId);
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
    
    @Override
    public MovieReviewResponse getUserReviewForMovie(String userUid, Long tmdbMovieId) {
        User user = getUserOrThrow(userUid);
        
        MovieReview review = reviewRepository.findByUserAndTmdbMovieId(user, tmdbMovieId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for movieId: " + tmdbMovieId));
        
        int commentCount = commentService.getCommentCountForReview(review.getId());
        return mapToMovieReviewResponse(review, commentCount);
    }
    
    @Override
    public Page<MovieReviewResponse> getFollowingReviews(String userUid, Pageable pageable) {
        User user = getUserOrThrow(userUid);
        
        log.info("Fetching reviews from users followed by: {}", userUid);
        return reviewRepository.findReviewsFromFollowedUsers(user, pageable)
                .map(review -> {
                    int commentCount = commentService.getCommentCountForReview(review.getId());
                    return mapToMovieReviewResponse(review, commentCount);
                });
    }
    
    @Override
    public Page<MovieReviewResponse> getPopularReviews(Pageable pageable) {
        log.info("Fetching popular reviews");
        return reviewRepository.findPopularReviews(pageable)
                .map(review -> {
                    int commentCount = commentService.getCommentCountForReview(review.getId());
                    return mapToMovieReviewResponse(review, commentCount);
                });
    }
    
    @Override
    public Page<MovieReviewResponse> getTrendingReviews(Pageable pageable) {
        // Get reviews from the last 7 days for trending
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        
        log.info("Fetching trending reviews since: {}", oneWeekAgo);
        
        // First, get all the reviews from the database
        Page<MovieReview> reviewsPage = reviewRepository.findTrendingReviews(oneWeekAgo, pageable);
        
        // Convert each MovieReview to a MovieReviewResponse with the comment count
        List<MovieReviewResponse> reviewResponses = reviewsPage.getContent().stream()
                .map(review -> {
                    int commentCount = commentService.getCommentCountForReview(review.getId());
                    return mapToMovieReviewResponseWithExtra(review, commentCount);
                })
                .collect(Collectors.toList());
        
        // Sort by our custom trending score (likes * 3 + comments)
        reviewResponses.sort((a, b) -> {
            int scoreA = a.getLikesCount() * 3 + a.getCommentCount();
            int scoreB = b.getLikesCount() * 3 + b.getCommentCount();
            if (scoreA != scoreB) {
                return Integer.compare(scoreB, scoreA); // Descending order
            } else {
                // If scores are equal, sort by creation date (newest first)
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            }
        });
        
        // Take only the requested number of items if we have too many
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), reviewResponses.size());
        
        if (start > reviewResponses.size()) {
            return new PageImpl<>(List.of(), pageable, reviewResponses.size());
        }
        
        List<MovieReviewResponse> pagedList = reviewResponses.subList(start, end);
        
        return new PageImpl<>(pagedList, pageable, reviewResponses.size());
    }
    
    private User getUserOrThrow(String userUid) {
        UserResponse user = userService.currentUser(userUid);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with UID: " + userUid);
        }
        return mapToUser(user);
    }
    
    private MovieReview getReviewOrThrow(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
    }
    
    private MovieReviewResponse mapToMovieReviewResponse(MovieReview review, int commentCount) {
        User user = review.getUser();
        return MovieReviewResponse.builder()
                .id(review.getId())
                .userUid(user.getFirebaseUid())
                .userName(user.getDisplayName())
                .userProfileImageUrl(user.getProfileImageUrl())
                .tmdbMovieId(review.getTmdbMovieId())
                .movieTitle(review.getMovieTitle())
                .reviewText(review.getReviewText())
                .rating(review.getRating())
                .containsSpoilers(review.isContainsSpoilers())
                .likesCount(review.getLikesCount())
                .commentCount(commentCount)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
    
    // Helper method to create a response with extra metadata for trending sorting
    private MovieReviewResponse mapToMovieReviewResponseWithExtra(MovieReview review, int commentCount) {
        return mapToMovieReviewResponse(review, commentCount);
    }

    private User mapToUser(UserResponse userResponse) {
        return User.builder()
                .firebaseUid(userResponse.getFirebaseUid())
                .email(userResponse.getEmail())
                .displayName(userResponse.getDisplayName())
                .profileImageUrl(userResponse.getProfileImageUrl())
                .createdAt(userResponse.getCreatedAt())
                .lastLogin(userResponse.getLastLogin())
                .build();
    }
}