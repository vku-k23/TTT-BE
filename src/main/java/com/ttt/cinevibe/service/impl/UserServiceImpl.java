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
    public UserResponse syncUser(UserRegisterRequest userRegisterRequest) {
        log.debug("Creating new user with Firebase UID: {}", userRegisterRequest.getFirebaseUid());

        boolean exists = userRepository.existsById(userRegisterRequest.getFirebaseUid());
        if (exists) {
            return currentUser(userRegisterRequest.getFirebaseUid());
        }

        User user = new User();
        user.setFirebaseUid(userRegisterRequest.getFirebaseUid());
        user.setEmail(userRegisterRequest.getEmail());
        user.setDisplayName(userRegisterRequest.getDisplayName() != null ? userRegisterRequest.getDisplayName() : userRegisterRequest.getEmail());
        user.setUsername(userRegisterRequest.getUsername());
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("Created new user with Firebase UID: {}", savedUser.getFirebaseUid());

        return mapToUserResponse(savedUser);
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
                .displayName(user.getDisplayName())
                .username(user.getUsername())
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