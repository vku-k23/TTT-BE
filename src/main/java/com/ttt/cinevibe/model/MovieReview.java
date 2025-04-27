package com.ttt.cinevibe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "movie_reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "tmdb_movie_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "tmdb_movie_id", nullable = false)
    private Long tmdbMovieId;
    
    @Column(name = "movie_title", nullable = false)
    private String movieTitle;
    
    @Column(name = "review_text", length = 5000)
    private String reviewText;
    
    @Column(name = "rating")
    private Float rating;
    
    @Column(name = "contains_spoilers", nullable = false)
    private boolean containsSpoilers;
    
    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}