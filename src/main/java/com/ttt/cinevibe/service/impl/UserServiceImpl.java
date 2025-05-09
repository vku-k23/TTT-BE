package com.ttt.cinevibe.service.impl;

import com.ttt.cinevibe.dto.request.UserProfileRequest;
import com.ttt.cinevibe.dto.request.UserRegisterRequest;
import com.ttt.cinevibe.dto.response.UserResponse;
import com.ttt.cinevibe.exception.ResourceNotFoundException;
import com.ttt.cinevibe.model.User;
import com.ttt.cinevibe.repository.UserRepository;
import com.ttt.cinevibe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse currentUser(String firebaseUid) {
        log.debug("Finding user with Firebase UID: {}", firebaseUid);
        User user = userRepository.findById(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UID: " + firebaseUid));

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse syncUser(UserRegisterRequest userRegisterRequest) {
        log.debug("Creating new user with Firebase UID: {}", userRegisterRequest.getFirebaseUid());
        log.info("User register request: {}", userRegisterRequest);

        boolean exists = userRepository.existsById(userRegisterRequest.getFirebaseUid());
        if (exists) {
            User existingUser = userRepository.findById(userRegisterRequest.getFirebaseUid()).get();
            
            // If the user exists and has a username, but the request doesn't have one, preserve the existing username
            if (existingUser.getUsername() != null && !existingUser.getUsername().isEmpty() 
                    && (userRegisterRequest.getUsername() == null || userRegisterRequest.getUsername().isEmpty())) {
                log.info("Using existing username: {} for user: {}", existingUser.getUsername(), existingUser.getFirebaseUid());
                return currentUser(userRegisterRequest.getFirebaseUid());
            }
            
            // Update last login time
            existingUser.setLastLogin(LocalDateTime.now());
            userRepository.save(existingUser);
            
            return currentUser(userRegisterRequest.getFirebaseUid());
        }

        User user = new User();
        user.setFirebaseUid(userRegisterRequest.getFirebaseUid());
        user.setEmail(userRegisterRequest.getEmail());
        user.setDisplayName(userRegisterRequest.getDisplayName() != null ? userRegisterRequest.getDisplayName() : userRegisterRequest.getEmail());
        
        // If no username is provided, generate one based on email
        if (userRegisterRequest.getUsername() == null || userRegisterRequest.getUsername().isEmpty()) {
            String baseUsername = generateUsernameFromEmail(userRegisterRequest.getEmail());
            String uniqueUsername = ensureUniqueUsername(baseUsername);
            log.info("Generated unique username: {} for user with email: {}", uniqueUsername, userRegisterRequest.getEmail());
            user.setUsername(uniqueUsername);
        } else {
            user.setUsername(userRegisterRequest.getUsername());
        }
        
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("Created new user with Firebase UID: {}", savedUser.getFirebaseUid());

        return mapToUserResponse(savedUser);
    }
    
    /**
     * Generate a username from an email address by taking the part before the @ symbol
     * and removing any non-alphanumeric characters
     */
    private String generateUsernameFromEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return "user" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        String localPart = email.split("@")[0];
        // Replace any non-alphanumeric characters with underscores and lowercase
        String username = localPart.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        
        // Ensure it's not empty and not too long
        if (username.isEmpty()) {
            username = "user";
        }
        
        if (username.length() > 15) {
            username = username.substring(0, 15);
        }
        
        return username;
    }
    
    /**
     * Ensure the username is unique by adding a number suffix if needed
     */
    private String ensureUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            if (username.length() + String.valueOf(counter).length() > 20) {
                // Truncate the base username if adding the counter would make it too long
                username = baseUsername.substring(0, 20 - String.valueOf(counter).length()) + counter;
            } else {
                username = baseUsername + counter;
            }
            counter++;
        }
        
        return username;
    }

    @Override
    @Transactional
    public UserResponse updateUserProfile(String firebaseUid, UserProfileRequest userProfileRequest) {

        log.info("Updating user with Firebase UID: {}", firebaseUid);
        log.info("User profile request: {}", userProfileRequest.getFirebaseUid());

        if(!firebaseUid.equals(userProfileRequest.getFirebaseUid())) {
            throw new IllegalArgumentException("Firebase UID in request does not match authenticated user.");
        }

        User existingUser = userRepository.findById(userProfileRequest.getFirebaseUid())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UID: " + userProfileRequest.getFirebaseUid()));

        if (userProfileRequest.getDisplayName() != null) {
            existingUser.setDisplayName(userProfileRequest.getDisplayName());
        }

        if (userProfileRequest.getUsername() != null) {
            if(userRepository.existsByUsername(userProfileRequest.getUsername())) {
                throw new IllegalArgumentException("Username already exists: " + userProfileRequest.getUsername());
            }
            existingUser.setUsername(userProfileRequest.getUsername());
        }

        if (userProfileRequest.getProfileImageUrl() != null) {
            existingUser.setProfileImageUrl(userProfileRequest.getProfileImageUrl());
        }

        if(userProfileRequest.getBio() != null) {
            existingUser.setBio(userProfileRequest.getBio());
        }

        if (userProfileRequest.getFavoriteGenre() != null) {
            existingUser.setFavoriteGenre(userProfileRequest.getFavoriteGenre());
        }

        existingUser.setLastLogin(LocalDateTime.now());

        User savedUser = userRepository.save(existingUser);
        log.info("Updated user with Firebase UID: {}", savedUser.getFirebaseUid());

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void updateLastLogin(String firebaseUid) {
        log.debug("Updating last login for user with Firebase UID: {}", firebaseUid);
        userRepository.findById(firebaseUid).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .firebaseUid(user.getFirebaseUid())
                .email(user.getEmail())
                .displayName(user.getDisplayName() != null ? user.getDisplayName() : user.getEmail())
                .username(user.getUsername() != null ? user.getUsername() : "")
                .profileImageUrl(user.getProfileImageUrl())
                .bio(user.getBio())
                .favoriteGenre(user.getFavoriteGenre())
                .reviewCount(user.getReviewCount())
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}