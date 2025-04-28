package com.ttt.cinevibe.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistItemRequest {
    
    @NotNull(message = "Movie ID is required")
    private Long tmdbMovieId;
    
    @NotNull(message = "Movie title is required")
    private String movieTitle;
    
    private String posterPath;
    
    private String addedNotes;
}