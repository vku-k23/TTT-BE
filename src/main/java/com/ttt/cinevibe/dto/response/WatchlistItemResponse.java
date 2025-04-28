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
public class WatchlistItemResponse {
    private Long id;
    private Long watchlistId;
    private String watchlistName;
    private Long tmdbMovieId;
    private String movieTitle;
    private String posterPath;
    private String addedNotes;
    private LocalDateTime addedAt;
}