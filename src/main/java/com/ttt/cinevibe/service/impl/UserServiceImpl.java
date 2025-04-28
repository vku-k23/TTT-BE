package com.ttt.cinevibe.service.impl;

import com.ttt.cinevibe.dto.request.UserRequest;
import com.ttt.cinevibe.dto.response.UserResponse;
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
    public UserResponse findByFirebaseUid(String firebaseUid) {
        log.debug("Finding user with Firebase UID: {}", firebaseUid);
        User user = userRepository.findById(firebaseUid).orElse(null);

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createOrUpdateUser(UserRequest userRequest) {
        log.debug("Creating or updating user with Firebase UID: {}", userRequest.getFirebaseUid());
        User user = userRepository.findById(userRequest.getFirebaseUid()).orElse(new User());

        user.setFirebaseUid(userRequest.getFirebaseUid());
        user.setEmail(userRequest.getEmail());
        user.setDisplayName(userRequest.getDisplayName() != null ? userRequest.getDisplayName() : userRequest.getEmail());
        user.setProfileImageUrl(userRequest.getProfileImageUrl());
        user.setLastLogin(LocalDateTime.now());

        User userSaved = userRepository.save(user);

        return mapToUserResponse(userSaved);
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
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}