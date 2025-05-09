package com.ttt.cinevibe.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileRequest {
    private String firebaseUid;
    private String displayName;
    private String username;
    private String profileImageUrl;
    private String bio;
    private String favoriteGenre;
    private String reviewCount;
    private String followersCount;
    private String followingCount;
}
