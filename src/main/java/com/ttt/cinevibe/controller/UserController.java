package com.ttt.cinevibe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ttt.cinevibe.dto.request.UserRequest;
import com.ttt.cinevibe.dto.response.UserResponse;
import com.ttt.cinevibe.service.UserService;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "User API", description = "Protected endpoints requiring Firebase authentication")
@RestController
@RequestMapping(value = "/api/user", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user info", description = "Returns the Firebase UID of the authenticated user")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        // The principal contains the Firebase UID set in FirebaseAuthenticationFilter
        String firebaseUid = (String) authentication.getPrincipal();

        UserResponse user = userService.findByFirebaseUid(firebaseUid);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    @PostMapping("/me")
    @Operation(summary = "Create or update user info", description = "Creates a new user or updates existing user info")
    public ResponseEntity<UserResponse> createOrUpdateUser(@RequestBody UserRequest userRequest) {
        UserResponse user = userService.createOrUpdateUser(userRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    @Operation(summary = "Test endpoint - development only", description = "Simulates the authenticated user endpoint for development testing")
    @GetMapping("/test/{uid}")
    public ResponseEntity<Map<String, String>> testGetUser(@PathVariable String uid) {
        Map<String, String> response = new HashMap<>();
        response.put("uid", uid);
        response.put("message", "This is a test endpoint for development only!");
        response.put("note", "In production, use /me endpoint with proper Firebase authentication");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}