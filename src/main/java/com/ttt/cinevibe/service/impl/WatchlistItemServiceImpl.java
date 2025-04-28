package com.ttt.cinevibe.service.impl;

import com.ttt.cinevibe.dto.request.WatchlistItemRequest;
import com.ttt.cinevibe.dto.response.WatchlistItemResponse;
import com.ttt.cinevibe.exception.ResourceNotFoundException;
import com.ttt.cinevibe.model.Watchlist;
import com.ttt.cinevibe.model.WatchlistItem;
import com.ttt.cinevibe.repository.WatchlistItemRepository;
import com.ttt.cinevibe.service.WatchlistItemService;
import com.ttt.cinevibe.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchlistItemServiceImpl implements WatchlistItemService {

    private final WatchlistItemRepository watchlistItemRepository;
    private final WatchlistService watchlistService;

    @Override
    public List<WatchlistItemResponse> getWatchlistItems(String userUid, Long watchlistId) {
        // This will throw exception if watchlist doesn't exist or user doesn't have access
        Watchlist watchlist = watchlistService.findWatchlistByIdAndUserUid(watchlistId, userUid);
        
        List<WatchlistItem> items = watchlistItemRepository.findByWatchlist(watchlist);
        return items.stream()
                .map(this::mapToWatchlistItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WatchlistItemResponse addToWatchlist(String userUid, Long watchlistId, WatchlistItemRequest request) {
        Watchlist watchlist = watchlistService.findWatchlistByIdAndUserUid(watchlistId, userUid);
        
        // Check if movie already exists in the watchlist
        if (watchlistItemRepository.existsByWatchlistAndTmdbMovieId(watchlist, request.getTmdbMovieId())) {
            log.info("Movie {} already exists in watchlist {}", request.getTmdbMovieId(), watchlistId);
            WatchlistItem existingItem = watchlistItemRepository.findByWatchlistAndTmdbMovieId(watchlist, request.getTmdbMovieId())
                    .orElseThrow(); // This shouldn't happen but just in case
            return mapToWatchlistItemResponse(existingItem);
        }
        
        WatchlistItem item = WatchlistItem.builder()
                .watchlist(watchlist)
                .tmdbMovieId(request.getTmdbMovieId())
                .movieTitle(request.getMovieTitle())
                .posterPath(request.getPosterPath())
                .addedNotes(request.getAddedNotes())
                .build();
        
        item = watchlistItemRepository.save(item);
        log.info("Added movie {} to watchlist {}", request.getTmdbMovieId(), watchlistId);
        
        return mapToWatchlistItemResponse(item);
    }

    @Override
    @Transactional
    public WatchlistItemResponse updateWatchlistItem(String userUid, Long watchlistId, Long itemId, WatchlistItemRequest request) {
        // Verify user has access to watchlist
        Watchlist watchlist = watchlistService.findWatchlistByIdAndUserUid(watchlistId, userUid);
        
        WatchlistItem item = watchlistItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist item not found with ID: " + itemId));
        
        // Ensure the item belongs to the specified watchlist
        if (!item.getWatchlist().getId().equals(watchlistId)) {
            throw new ResourceNotFoundException("Item with ID: " + itemId + " not found in watchlist: " + watchlistId);
        }
        
        // Update only the notes, not the movie details
        item.setAddedNotes(request.getAddedNotes());
        
        item = watchlistItemRepository.save(item);
        log.info("Updated item {} in watchlist {}", itemId, watchlistId);
        
        return mapToWatchlistItemResponse(item);
    }

    @Override
    @Transactional
    public void removeFromWatchlist(String userUid, Long watchlistId, Long itemId) {
        // Verify user has access to watchlist
        Watchlist watchlist = watchlistService.findWatchlistByIdAndUserUid(watchlistId, userUid);
        
        WatchlistItem item = watchlistItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist item not found with ID: " + itemId));
        
        // Ensure the item belongs to the specified watchlist
        if (!item.getWatchlist().getId().equals(watchlistId)) {
            throw new ResourceNotFoundException("Item with ID: " + itemId + " not found in watchlist: " + watchlistId);
        }
        
        watchlistItemRepository.delete(item);
        log.info("Removed item {} from watchlist {}", itemId, watchlistId);
    }

    @Override
    @Transactional
    public void removeMovieFromWatchlist(String userUid, Long watchlistId, Long tmdbMovieId) {
        Watchlist watchlist = watchlistService.findWatchlistByIdAndUserUid(watchlistId, userUid);
        
        if (!watchlistItemRepository.existsByWatchlistAndTmdbMovieId(watchlist, tmdbMovieId)) {
            throw new ResourceNotFoundException("Movie with ID: " + tmdbMovieId + " not found in watchlist: " + watchlistId);
        }
        
        watchlistItemRepository.deleteByWatchlistAndTmdbMovieId(watchlist, tmdbMovieId);
        log.info("Removed movie {} from watchlist {}", tmdbMovieId, watchlistId);
    }

    @Override
    public boolean isMovieInWatchlist(String userUid, Long watchlistId, Long tmdbMovieId) {
        try {
            Watchlist watchlist = watchlistService.findWatchlistByIdAndUserUid(watchlistId, userUid);
            return watchlistItemRepository.existsByWatchlistAndTmdbMovieId(watchlist, tmdbMovieId);
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
    
    private WatchlistItemResponse mapToWatchlistItemResponse(WatchlistItem item) {
        return WatchlistItemResponse.builder()
                .id(item.getId())
                .watchlistId(item.getWatchlist().getId())
                .watchlistName(item.getWatchlist().getName())
                .tmdbMovieId(item.getTmdbMovieId())
                .movieTitle(item.getMovieTitle())
                .posterPath(item.getPosterPath())
                .addedNotes(item.getAddedNotes())
                .addedAt(item.getAddedAt())
                .build();
    }
}