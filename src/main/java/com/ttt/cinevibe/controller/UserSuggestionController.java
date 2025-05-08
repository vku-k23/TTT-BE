package com.ttt.cinevibe.controller;

import com.ttt.cinevibe.dto.response.UserSuggestionResponse;
import com.ttt.cinevibe.service.UserSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Suggestion API", description = "Endpoints to get user suggestions")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/suggestions")
public class UserSuggestionController {

    private final UserSuggestionService userSuggestionService;

    @Operation(summary = "Get suggested people to follow", 
              description = "Returns a list of suggested users based on similar interests, common connections, and activity")
    @GetMapping("/people")
    public ResponseEntity<Page<UserSuggestionResponse>> getSuggestedPeople(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(userSuggestionService.suggestPeople(userUid, pageable));
    }

    @Operation(summary = "Update suggestion follow status", 
              description = "Records whether a suggested user was followed")
    @PutMapping("/follow-status")
    public ResponseEntity<Void> updateSuggestionFollowStatus(
            @RequestParam String suggestedUserId,
            @RequestParam boolean wasFollowed,
            Authentication authentication) {
        String userUid = authentication.getName();
        userSuggestionService.updateSuggestionFollowStatus(userUid, suggestedUserId, wasFollowed);
        return ResponseEntity.noContent().build();
    }
}