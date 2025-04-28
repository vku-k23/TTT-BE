package com.ttt.cinevibe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieReviewResponse {
    private Long id;
    private String userUid;
    private String userName;
    private String userProfileImageUrl;
    private Long tmdbMovieId;
    private String movieTitle;
    private String reviewText;
    private Float rating;
    private boolean containsSpoilers;
    private Integer likesCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}