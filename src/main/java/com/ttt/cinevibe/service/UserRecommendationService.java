package com.ttt.cinevibe.service;

import com.ttt.cinevibe.dto.response.UserProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRecommendationService {
    
    /**
     * Get a list of recommended users to follow based on various factors
     * - Users with similar movie interests
     * - Popular users
     * - New users
     * - Excluding users already followed
     */
    Page<UserProfileResponse> getRecommendedUsers(String currentUserUid, Pageable pageable);
    
    /**
     * Get a specific user profile by Firebase UID
     * Includes connection status with current user
     */
    UserProfileResponse getUserProfile(String currentUserUid, String targetUserUid);
    
    /**
     * Search for users by username or display name
     */
    Page<UserProfileResponse> searchUsers(String currentUserUid, String query, Pageable pageable);
} 