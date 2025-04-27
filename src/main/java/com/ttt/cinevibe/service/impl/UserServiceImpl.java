package com.ttt.cinevibe.service.impl;

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
    public User findByFirebaseUid(String firebaseUid) {
        log.debug("Finding user with Firebase UID: {}", firebaseUid);
        return userRepository.findById(firebaseUid).orElse(null);
    }

    @Override
    @Transactional
    public User createOrUpdateUser(String firebaseUid, String email, String displayName, String profileImageUrl) {
        log.debug("Creating or updating user with Firebase UID: {}", firebaseUid);
        User user = userRepository.findById(firebaseUid).orElse(new User());

        user.setFirebaseUid(firebaseUid);
        user.setEmail(email);
        user.setDisplayName(displayName != null ? displayName : email);
        user.setProfileImageUrl(profileImageUrl);
        user.setLastLogin(LocalDateTime.now());

        return userRepository.save(user);
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
}