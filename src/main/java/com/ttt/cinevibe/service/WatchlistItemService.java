package com.ttt.cinevibe.service;

import com.ttt.cinevibe.dto.request.WatchlistItemRequest;
import com.ttt.cinevibe.dto.response.WatchlistItemResponse;

import java.util.List;

public interface WatchlistItemService {
    
    List<WatchlistItemResponse> getWatchlistItems(String userUid, Long watchlistId);
    
    WatchlistItemResponse addToWatchlist(String userUid, Long watchlistId, WatchlistItemRequest request);
    
    WatchlistItemResponse updateWatchlistItem(String userUid, Long watchlistId, Long itemId, WatchlistItemRequest request);
    
    void removeFromWatchlist(String userUid, Long watchlistId, Long itemId);
    
    void removeMovieFromWatchlist(String userUid, Long watchlistId, Long tmdbMovieId);
    
    boolean isMovieInWatchlist(String userUid, Long watchlistId, Long tmdbMovieId);
}