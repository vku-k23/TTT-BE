package com.ttt.cinevibe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ttt.cinevibe.dto.request.UserProfileRequest;
import com.ttt.cinevibe.dto.request.UserRegisterRequest;
import com.ttt.cinevibe.dto.response.UserResponse;
import com.ttt.cinevibe.service.UserService;

import jakarta.validation.Valid;

@Tag(name = "User API", description = "Protected endpoints requiring Firebase authentication")
@RestController
@RequestMapping(value = "/api/user", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user info", description = "Returns the current authenticated user's information")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String firebaseUid = (String) authentication.getPrincipal();

        UserResponse user = userService.currentUser(firebaseUid);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    @Operation(summary = "Register new user", description = "Creates a new user account")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        UserResponse user = userService.syncUser(userRegisterRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    @Operation(summary = "Update user profile", description = "Updates the current user's profile information")
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateUserProfile(
            @Valid @RequestBody UserProfileRequest profileRequest,
            Authentication authentication) {
//        String firebaseUid = (String) authentication.getPrincipal();
        UserResponse user = userService.updateUserProfile(profileRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

}