package com.ttt.cinevibe.service;

import com.ttt.cinevibe.dto.response.UserProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRecommendationService {

    Page<UserProfileResponse> getRecommendedUsers(String currentUserUid, Pageable pageable);
    
    UserProfileResponse getUserProfile(String currentUserUid, String targetUserUid);
    
    Page<UserProfileResponse> searchUsers(String currentUserUid, String query, Pageable pageable);
} 