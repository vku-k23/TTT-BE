package com.ttt.cinevibe.service;

import com.ttt.cinevibe.dto.request.WatchlistRequest;
import com.ttt.cinevibe.dto.response.WatchlistResponse;
import com.ttt.cinevibe.model.Watchlist;

import java.util.List;

public interface WatchlistService {
    
    List<WatchlistResponse> getUserWatchlists(String userUid);
    
    List<WatchlistResponse> getPublicWatchlists();
    
    WatchlistResponse getWatchlistById(String userUid, Long watchlistId);
    
    WatchlistResponse createWatchlist(String userUid, WatchlistRequest request);
    
    WatchlistResponse updateWatchlist(String userUid, Long watchlistId, WatchlistRequest request);
    
    void deleteWatchlist(String userUid, Long watchlistId);
    
    Watchlist findWatchlistByIdAndUserUid(Long watchlistId, String userUid);
}