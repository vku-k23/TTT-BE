package com.ttt.cinevibe.service.impl;

import com.ttt.cinevibe.dto.response.UserProfileResponse;
import com.ttt.cinevibe.dto.response.UserResponse;
import com.ttt.cinevibe.exception.ResourceNotFoundException;
import com.ttt.cinevibe.model.User;
import com.ttt.cinevibe.model.UserConnection;
import com.ttt.cinevibe.model.UserConnection.ConnectionStatus;
import com.ttt.cinevibe.repository.UserConnectionRepository;
import com.ttt.cinevibe.repository.UserRepository;
import com.ttt.cinevibe.service.UserRecommendationService;
import com.ttt.cinevibe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRecommendationServiceImpl implements UserRecommendationService {

    private final UserRepository userRepository;
    private final UserConnectionRepository connectionRepository;
    private final UserService userService;

    @Override
    public Page<UserProfileResponse> getRecommendedUsers(String currentUserUid, Pageable pageable) {
        User currentUser = getUserOrThrow(currentUserUid);
        
        // Find all users the current user is already following
        List<UserConnection> following = connectionRepository.findByFollowerAndStatus(
                currentUser, ConnectionStatus.ACCEPTED);
        
        List<String> followedUserIds = following.stream()
                .map(f -> f.getFollowing().getFirebaseUid())
                .collect(Collectors.toList());
        
        // Add current user's ID to exclude from recommendations
        followedUserIds.add(currentUserUid);
        
        // Get all users except those the current user is already following
        // In a real app, we'd add more sophisticated recommendation logic
        List<User> allUsers = userRepository.findAll();
        List<User> recommendedUsers = allUsers.stream()
                .filter(user -> !followedUserIds.contains(user.getFirebaseUid()))
                .collect(Collectors.toList());
        
        // Select users for the current page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), recommendedUsers.size());
        
        List<User> pageUsers = new ArrayList<>();
        if (start <= end) {
            pageUsers = recommendedUsers.subList(start, end);
        }
        
        // Convert to UserProfileResponse objects
        List<UserProfileResponse> responseList = pageUsers.stream()
                .map(user -> {
                    UserResponse userResponse = mapToUserResponse(user);
                    return UserProfileResponse.fromUserResponse(
                            userResponse, 
                            null, 
                            false);
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(responseList, pageable, recommendedUsers.size());
    }

    @Override
    public UserProfileResponse getUserProfile(String currentUserUid, String targetUserUid) {
        User currentUser = getUserOrThrow(currentUserUid);
        User targetUser = getUserOrThrow(targetUserUid);
        
        boolean isCurrentUser = currentUserUid.equals(targetUserUid);
        ConnectionStatus connectionStatus = null;
        
        if (!isCurrentUser) {
            // Check if there's a connection between users
            Optional<UserConnection> connection = connectionRepository
                    .findByFollowerAndFollowing(currentUser, targetUser);
            
            if (connection.isPresent()) {
                connectionStatus = connection.get().getStatus();
            }
        }
        
        UserResponse userResponse = mapToUserResponse(targetUser);
        return UserProfileResponse.fromUserResponse(userResponse, connectionStatus, isCurrentUser);
    }

    @Override
    public Page<UserProfileResponse> searchUsers(String currentUserUid, String query, Pageable pageable) {
        User currentUser = getUserOrThrow(currentUserUid);
        
        if (query == null || query.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Use the repository method to search for users
        Page<User> searchResults = userRepository.searchByUsernameOrDisplayName(query, pageable);
        
        // Convert to UserProfileResponse objects
        return searchResults.map(user -> {
            UserResponse userResponse = mapToUserResponse(user);
            boolean isCurrentUser = user.getFirebaseUid().equals(currentUserUid);
            ConnectionStatus connectionStatus = null;
            
            if (!isCurrentUser) {
                // Check if there's a connection between users
                Optional<UserConnection> connection = connectionRepository
                        .findByFollowerAndFollowing(currentUser, user);
                
                if (connection.isPresent()) {
                    connectionStatus = connection.get().getStatus();
                }
            }
            
            return UserProfileResponse.fromUserResponse(
                    userResponse, 
                    connectionStatus, 
                    isCurrentUser);
        });
    }
    
    // Helper method to get a User or throw exception
    private User getUserOrThrow(String firebaseUid) {
        return userRepository.findById(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UID: " + firebaseUid));
    }
    
    // Map User entity to UserResponse DTO
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .firebaseUid(user.getFirebaseUid())
                .email(user.getEmail())
                .displayName(user.getDisplayName() != null ? user.getDisplayName() : user.getEmail())
                .username(user.getUsername() != null ? user.getUsername() : "")
                .profileImageUrl(user.getProfileImageUrl())
                .bio(user.getBio())
                .favoriteGenre(user.getFavoriteGenre())
                .reviewCount(user.getReviewCount())
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
} 