package com.ttt.cinevibe.controller;

import com.ttt.cinevibe.dto.request.WatchlistRequest;
import com.ttt.cinevibe.dto.response.WatchlistResponse;
import com.ttt.cinevibe.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Watchlist API", description = "Endpoints to manage movie watchlists")
@RestController
@RequestMapping("/api/watchlists")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @Operation(summary = "Get user's watchlists", description = "Returns all watchlists belonging to the authenticated user")
    @GetMapping("/my")
    public ResponseEntity<List<WatchlistResponse>> getUserWatchlists(Authentication authentication) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(watchlistService.getUserWatchlists(userUid));
    }

    @Operation(summary = "Get all public watchlists", description = "Returns all public watchlists from all users")
    @GetMapping("/public")
    public ResponseEntity<List<WatchlistResponse>> getPublicWatchlists() {
        return ResponseEntity.ok(watchlistService.getPublicWatchlists());
    }

    @Operation(summary = "Get watchlist by ID", description = "Returns a specific watchlist if the user has access")
    @GetMapping("/{watchlistId}")
    public ResponseEntity<WatchlistResponse> getWatchlistById(
            @PathVariable Long watchlistId,
            Authentication authentication) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(watchlistService.getWatchlistById(userUid, watchlistId));
    }

    @Operation(summary = "Create a new watchlist", description = "Creates a new watchlist for the authenticated user")
    @PostMapping
    public ResponseEntity<WatchlistResponse> createWatchlist(
            @Valid @RequestBody WatchlistRequest request,
            Authentication authentication) {
        String userUid = authentication.getName();
        WatchlistResponse created = watchlistService.createWatchlist(userUid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a watchlist", description = "Updates an existing watchlist owned by the authenticated user")
    @PutMapping("/{watchlistId}")
    public ResponseEntity<WatchlistResponse> updateWatchlist(
            @PathVariable Long watchlistId,
            @Valid @RequestBody WatchlistRequest request,
            Authentication authentication) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(watchlistService.updateWatchlist(userUid, watchlistId, request));
    }

    @Operation(summary = "Delete a watchlist", description = "Deletes a watchlist owned by the authenticated user")
    @DeleteMapping("/{watchlistId}")
    public ResponseEntity<Void> deleteWatchlist(
            @PathVariable Long watchlistId,
            Authentication authentication) {
        String userUid = authentication.getName();
        watchlistService.deleteWatchlist(userUid, watchlistId);
        return ResponseEntity.noContent().build();
    }
}