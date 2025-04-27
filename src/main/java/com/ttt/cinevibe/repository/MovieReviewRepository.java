package com.ttt.cinevibe.repository;

import com.ttt.cinevibe.model.MovieReview;
import com.ttt.cinevibe.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}