package com.ttt.cinevibe.dto.response;

import com.ttt.cinevibe.model.UserConnection.ConnectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    // UserResponse fields
    private String firebaseUid;
    private String displayName;
    private String username;
    private String email;
    private String profileImageUrl;
    private String bio;
    private String favoriteGenre;
    private Integer reviewCount;
    private Integer followersCount;
    private Integer followingCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    
    // Additional fields for profile view
    private ConnectionStatus connectionStatus;
    private boolean isCurrentUser;
    
    // Factory method to create from a UserResponse
    public static UserProfileResponse fromUserResponse(UserResponse userResponse, ConnectionStatus connectionStatus, boolean isCurrentUser) {
        return UserProfileResponse.builder()
                .firebaseUid(userResponse.getFirebaseUid())
                .displayName(userResponse.getDisplayName())
                .username(userResponse.getUsername())
                .email(userResponse.getEmail())
                .profileImageUrl(userResponse.getProfileImageUrl())
                .bio(userResponse.getBio())
                .favoriteGenre(userResponse.getFavoriteGenre())
                .reviewCount(userResponse.getReviewCount())
                .followersCount(userResponse.getFollowersCount())
                .followingCount(userResponse.getFollowingCount())
                .createdAt(userResponse.getCreatedAt())
                .lastLogin(userResponse.getLastLogin())
                .connectionStatus(connectionStatus)
                .isCurrentUser(isCurrentUser)
                .build();
    }
} 