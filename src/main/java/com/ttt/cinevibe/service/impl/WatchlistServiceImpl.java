package com.ttt.cinevibe.service.impl;

import com.ttt.cinevibe.dto.request.WatchlistRequest;
import com.ttt.cinevibe.dto.response.UserResponse;
import com.ttt.cinevibe.dto.response.WatchlistResponse;
import com.ttt.cinevibe.exception.ResourceNotFoundException;
import com.ttt.cinevibe.exception.UnauthorizedException;
import com.ttt.cinevibe.model.User;
import com.ttt.cinevibe.model.Watchlist;
import com.ttt.cinevibe.repository.WatchlistItemRepository;
import com.ttt.cinevibe.repository.WatchlistRepository;
import com.ttt.cinevibe.service.UserService;
import com.ttt.cinevibe.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchlistServiceImpl implements WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistItemRepository watchlistItemRepository;
    private final UserService userService;

    @Override
    public List<WatchlistResponse> getUserWatchlists(String userUid) {
        User user = getUserOrThrow(userUid);
        List<Watchlist> watchlists = watchlistRepository.findByUser(user);
        return watchlists.stream()
                .map(this::mapToWatchlistResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WatchlistResponse> getPublicWatchlists() {
        List<Watchlist> publicWatchlists = watchlistRepository.findByIsPublicTrue();
        return publicWatchlists.stream()
                .map(this::mapToWatchlistResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WatchlistResponse getWatchlistById(String userUid, Long watchlistId) {
        Watchlist watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found with ID: " + watchlistId));
        
        // Check if watchlist is public or belongs to the requesting user
        if (!watchlist.isPublic() && !watchlist.getUser().getFirebaseUid().equals(userUid)) {
            throw new UnauthorizedException("You don't have permission to view this watchlist");
        }
        
        return mapToWatchlistResponse(watchlist);
    }

    @Override
    @Transactional
    public WatchlistResponse createWatchlist(String userUid, WatchlistRequest request) {
        User user = getUserOrThrow(userUid);
        
        Watchlist watchlist = Watchlist.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isPublic(request.isPublic())
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        watchlist = watchlistRepository.save(watchlist);
        log.info("Created new watchlist with ID: {} for user: {}", watchlist.getId(), userUid);
        
        return mapToWatchlistResponse(watchlist);
    }

    @Override
    @Transactional
    public WatchlistResponse updateWatchlist(String userUid, Long watchlistId, WatchlistRequest request) {
        Watchlist watchlist = findWatchlistByIdAndUserUid(watchlistId, userUid);
        
        watchlist.setName(request.getName());
        watchlist.setDescription(request.getDescription());
        watchlist.setPublic(request.isPublic());
        watchlist.setUpdatedAt(LocalDateTime.now());
        
        watchlist = watchlistRepository.save(watchlist);
        log.info("Updated watchlist with ID: {} for user: {}", watchlistId, userUid);
        
        return mapToWatchlistResponse(watchlist);
    }

    @Override
    @Transactional
    public void deleteWatchlist(String userUid, Long watchlistId) {
        Watchlist watchlist = findWatchlistByIdAndUserUid(watchlistId, userUid);
        watchlistRepository.delete(watchlist);
        log.info("Deleted watchlist with ID: {} for user: {}", watchlistId, userUid);
    }

    @Override
    public Watchlist findWatchlistByIdAndUserUid(Long watchlistId, String userUid) {
        User user = getUserOrThrow(userUid);
        return watchlistRepository.findByIdAndUser(watchlistId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found with ID: " + watchlistId + " for user: " + userUid));
    }
    
    private User getUserOrThrow(String userUid) {
        UserResponse user = userService.findByFirebaseUid(userUid);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with UID: " + userUid);
        }
        return mapToUser(user);
    }
    
    private WatchlistResponse mapToWatchlistResponse(Watchlist watchlist) {
        // Count items in the watchlist
        long itemCount = watchlistItemRepository.countByWatchlist(watchlist);
        
        return WatchlistResponse.builder()
                .id(watchlist.getId())
                .name(watchlist.getName())
                .description(watchlist.getDescription())
                .isPublic(watchlist.isPublic())
                .ownerUid(watchlist.getUser().getFirebaseUid())
                .ownerName(watchlist.getUser().getDisplayName())
                .itemCount((int) itemCount)
                .createdAt(watchlist.getCreatedAt())
                .updatedAt(watchlist.getUpdatedAt())
                .build();
    }

    private User mapToUser(UserResponse userResponse) {
        return User.builder()
                .firebaseUid(userResponse.getFirebaseUid())
                .email(userResponse.getEmail())
                .displayName(userResponse.getDisplayName())
                .profileImageUrl(userResponse.getProfileImageUrl())
                .createdAt(userResponse.getCreatedAt())
                .lastLogin(userResponse.getLastLogin())
                .build();
    }
}