package com.ttt.cinevibe.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieReviewRequest {
    
    @NotNull(message = "Movie ID is required")
    private Long tmdbMovieId;
    
    @NotNull(message = "Movie title is required")
    private String movieTitle;
    
    @Size(max = 5000, message = "Review text cannot exceed 5000 characters")
    private String reviewText;
    
    @Min(value = 0, message = "Rating must be at least 0")
    @Max(value = 10, message = "Rating cannot exceed 10")
    private Float rating;
    
    private boolean containsSpoilers = false;
}