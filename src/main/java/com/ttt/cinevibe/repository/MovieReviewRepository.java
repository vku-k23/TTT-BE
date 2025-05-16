package com.ttt.cinevibe.repository;

import com.ttt.cinevibe.model.MovieReview;
import com.ttt.cinevibe.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieReviewRepository extends JpaRepository<MovieReview, Long> {
    List<MovieReview> findByUser(User user);
    Page<MovieReview> findByUser(User user, Pageable pageable);
    List<MovieReview> findByTmdbMovieId(Long tmdbMovieId);
    Page<MovieReview> findByTmdbMovieId(Long tmdbMovieId, Pageable pageable);
    Optional<MovieReview> findByUserAndTmdbMovieId(User user, Long tmdbMovieId);
    boolean existsByUserAndTmdbMovieId(User user, Long tmdbMovieId);
    
    // Find reviews from users that a given user follows
    @Query("SELECT r FROM MovieReview r WHERE r.user IN " +
           "(SELECT uc.following FROM UserConnection uc WHERE uc.follower = :user AND uc.status = 'ACCEPTED') " +
           "ORDER BY r.createdAt DESC")
    Page<MovieReview> findReviewsFromFollowedUsers(@Param("user") User user, Pageable pageable);
    
    // Find popular reviews based on like count
    @Query("SELECT r FROM MovieReview r ORDER BY r.likesCount DESC, r.createdAt DESC")
    Page<MovieReview> findPopularReviews(Pageable pageable);
    
    // Find trending reviews - recent with good engagement
    @Query("SELECT r FROM MovieReview r WHERE r.createdAt >= :since " +
           "ORDER BY r.likesCount DESC, r.createdAt DESC")
    Page<MovieReview> findTrendingReviews(@Param("since") LocalDateTime since, Pageable pageable);
}