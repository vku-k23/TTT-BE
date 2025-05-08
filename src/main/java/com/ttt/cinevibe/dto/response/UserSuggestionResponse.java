package com.ttt.cinevibe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSuggestionResponse {
    private String userId;
    private String displayName;
    private String profileImageUrl;
    private String bio;
    private String favoriteGenre;
    private Integer followersCount;
    private Integer followingCount;
    private Integer reviewCount;
    private String reasonForSuggestion;
    private Double matchScore;
}