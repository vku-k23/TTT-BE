package com.ttt.cinevibe.controller;

import com.ttt.cinevibe.dto.request.UserConnectionRequest;
import com.ttt.cinevibe.dto.response.UserConnectionResponse;
import com.ttt.cinevibe.model.UserConnection.ConnectionStatus;
import com.ttt.cinevibe.service.UserConnectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "User Connection API", description = "Endpoints to manage user connections (follows, friendships)")
@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class UserConnectionController {

    private final UserConnectionService connectionService;

    @Operation(summary = "Get users I follow", description = "Returns all users the authenticated user follows")
    @GetMapping("/following")
    public ResponseEntity<Page<UserConnectionResponse>> getFollowing(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(connectionService.getFollowing(userUid, pageable));
    }

    @Operation(summary = "Get my followers", description = "Returns all users who follow the authenticated user")
    @GetMapping("/followers")
    public ResponseEntity<Page<UserConnectionResponse>> getFollowers(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(connectionService.getFollowers(userUid, pageable));
    }

    @Operation(summary = "Get pending follow requests", description = "Returns all pending follow requests for the authenticated user")
    @GetMapping("/pending")
    public ResponseEntity<Page<UserConnectionResponse>> getPendingRequests(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(connectionService.getPendingRequests(userUid, pageable));
    }

    @Operation(summary = "Follow a user", description = "Create a follow request to another user")
    @PostMapping("/follow")
    public ResponseEntity<UserConnectionResponse> followUser(
            @Valid @RequestBody UserConnectionRequest request,
            Authentication authentication) {
        String userUid = authentication.getName();
        UserConnectionResponse created = connectionService.followUser(userUid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Accept follow request", description = "Accept a pending follow request")
    @PostMapping("/{connectionId}/accept")
    public ResponseEntity<UserConnectionResponse> acceptFollowRequest(
            @PathVariable Long connectionId,
            Authentication authentication) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(connectionService.acceptFollowRequest(userUid, connectionId));
    }

    @Operation(summary = "Reject follow request", description = "Reject a pending follow request")
    @PostMapping("/{connectionId}/reject")
    public ResponseEntity<Void> rejectFollowRequest(
            @PathVariable Long connectionId,
            Authentication authentication) {
        String userUid = authentication.getName();
        connectionService.rejectFollowRequest(userUid, connectionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Unfollow user", description = "Stop following a user")
    @DeleteMapping("/unfollow/{targetUserUid}")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable String targetUserUid,
            Authentication authentication) {
        String userUid = authentication.getName();
        connectionService.unfollowUser(userUid, targetUserUid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove follower", description = "Remove a user from your followers")
    @DeleteMapping("/followers/{followerUid}")
    public ResponseEntity<Void> removeFollower(
            @PathVariable String followerUid,
            Authentication authentication) {
        String userUid = authentication.getName();
        connectionService.removeFollower(userUid, followerUid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Check if following user", description = "Verifies if the authenticated user follows a specified user")
    @GetMapping("/check/{targetUserUid}")
    public ResponseEntity<Map<String, Object>> checkConnectionStatus(
            @PathVariable String targetUserUid,
            Authentication authentication) {
        String userUid = authentication.getName();
        boolean isFollowing = connectionService.isFollowing(userUid, targetUserUid);
        ConnectionStatus status = connectionService.getConnectionStatus(userUid, targetUserUid);
        
        return ResponseEntity.ok(Map.of(
            "isFollowing", isFollowing,
            "status", status != null ? status.toString() : "NONE"
        ));
    }
}