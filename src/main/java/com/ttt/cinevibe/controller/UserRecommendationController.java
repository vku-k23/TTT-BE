package com.ttt.cinevibe.controller;

import com.ttt.cinevibe.dto.response.UserProfileResponse;
import com.ttt.cinevibe.dto.response.UserResponse;
import com.ttt.cinevibe.service.UserRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Recommendation API", description = "API for recommending users to follow")
@RestController
@RequestMapping(value = "/api/users", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class UserRecommendationController {

    private final UserRecommendationService userRecommendationService;

    @Operation(summary = "Get recommended users", description = "Returns a list of recommended users to follow based on various factors")
    @GetMapping("/recommendations")
    public ResponseEntity<Page<UserProfileResponse>> getRecommendedUsers(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        String firebaseUid = (String) authentication.getPrincipal();
        
        Page<UserProfileResponse> recommendedUsers = userRecommendationService.getRecommendedUsers(firebaseUid, pageable);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(recommendedUsers);
    }
    
    @Operation(summary = "Get user profile by ID", description = "Returns the profile of a specific user by their ID")
    @GetMapping("/{firebaseUid}")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            Authentication authentication,
            @PathVariable String firebaseUid) {
        String currentUserUid = (String) authentication.getPrincipal();
        
        UserProfileResponse userProfile = userRecommendationService.getUserProfile(currentUserUid, firebaseUid);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userProfile);
    }
    
    @Operation(summary = "Search users", description = "Search for users by username or display name")
    @GetMapping("/search")
    public ResponseEntity<Page<UserProfileResponse>> searchUsers(
            Authentication authentication,
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable) {
        String firebaseUid = (String) authentication.getPrincipal();
        
        Page<UserProfileResponse> searchResults = userRecommendationService.searchUsers(firebaseUid, query, pageable);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(searchResults);
    }
} 