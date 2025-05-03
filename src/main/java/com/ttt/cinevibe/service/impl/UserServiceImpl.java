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
    public UserResponse createUser(UserRegisterRequest userRegisterRequest) {
        log.debug("Creating new user with Firebase UID: {}", userRegisterRequest.getFirebaseUid());

        // Check if user already exists
        if (userRepository.existsById(userRegisterRequest.getFirebaseUid())) {
            throw new IllegalStateException("User with this UID already exists");
        }

        User user = new User();
        user.setFirebaseUid(userRegisterRequest.getFirebaseUid());
        user.setEmail(userRegisterRequest.getEmail());
        user.setDisplayName(userRegisterRequest.getDisplayName() != null ? userRegisterRequest.getDisplayName() : userRegisterRequest.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("Created new user with Firebase UID: {}", savedUser.getFirebaseUid());

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserProfile(UserProfileRequest userProfileRequest) {

        User existingUser = userRepository.findById(userProfileRequest.getFirebaseUid())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UID: " + userProfileRequest.getFirebaseUid()));

        // Update user fields
        if (userProfileRequest.getDisplayName() != null) {
            existingUser.setDisplayName(userProfileRequest.getDisplayName());
        }

        if (userProfileRequest.getProfileImageUrl() != null) {
            existingUser.setProfileImageUrl(userProfileRequest.getProfileImageUrl());
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
                .displayName(user.getDisplayName())
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