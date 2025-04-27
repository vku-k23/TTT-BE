package com.ttt.cinevibe.controller;

import com.ttt.cinevibe.model.User;
import com.ttt.cinevibe.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User API", description = "Protected endpoints requiring Firebase authentication")
@RestController
@RequestMapping(value = "/api/user", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user info", description = "Returns the profile information for the authenticated user")
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String firebaseUid = (String) authentication.getPrincipal();
        User user = userService.findByFirebaseUid(firebaseUid);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }
}