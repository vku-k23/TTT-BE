package com.ttt.cinevibe.controller;

import com.ttt.cinevibe.dto.request.WatchlistItemRequest;
import com.ttt.cinevibe.dto.response.WatchlistItemResponse;
import com.ttt.cinevibe.service.WatchlistItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Watchlist Items API", description = "Endpoints to manage movies in watchlists")
@RestController
@RequestMapping("/api/watchlists/{watchlistId}/items")
@RequiredArgsConstructor
public class WatchlistItemController {

    private final WatchlistItemService watchlistItemService;

    @Operation(summary = "Get all items in a watchlist", description = "Returns all movies in a specific watchlist")
    @GetMapping
    public ResponseEntity<List<WatchlistItemResponse>> getWatchlistItems(
            @PathVariable Long watchlistId,
            Authentication authentication) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(watchlistItemService.getWatchlistItems(userUid, watchlistId));
    }

    @Operation(summary = "Add a movie to watchlist", description = "Adds a movie to the specified watchlist")
    @PostMapping
    public ResponseEntity<WatchlistItemResponse> addToWatchlist(
            @PathVariable Long watchlistId,
            @Valid @RequestBody WatchlistItemRequest request,
            Authentication authentication) {
        String userUid = authentication.getName();
        WatchlistItemResponse created = watchlistItemService.addToWatchlist(userUid, watchlistId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a watchlist item", description = "Updates details like notes for a watchlist item")
    @PutMapping("/{itemId}")
    public ResponseEntity<WatchlistItemResponse> updateWatchlistItem(
            @PathVariable Long watchlistId,
            @PathVariable Long itemId,
            @Valid @RequestBody WatchlistItemRequest request,
            Authentication authentication) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(watchlistItemService.updateWatchlistItem(userUid, watchlistId, itemId, request));
    }

    @Operation(summary = "Remove a movie from watchlist", description = "Removes a specific movie from the watchlist")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeFromWatchlist(
            @PathVariable Long watchlistId,
            @PathVariable Long itemId,
            Authentication authentication) {
        String userUid = authentication.getName();
        watchlistItemService.removeFromWatchlist(userUid, watchlistId, itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Check if movie is in watchlist", description = "Verifies if a movie exists in the watchlist")
    @GetMapping("/check/{tmdbMovieId}")
    public ResponseEntity<Map<String, Boolean>> checkMovieInWatchlist(
            @PathVariable Long watchlistId,
            @PathVariable Long tmdbMovieId,
            Authentication authentication) {
        String userUid = authentication.getName();
        boolean isInWatchlist = watchlistItemService.isMovieInWatchlist(userUid, watchlistId, tmdbMovieId);
        return ResponseEntity.ok(Map.of("inWatchlist", isInWatchlist));
    }
}