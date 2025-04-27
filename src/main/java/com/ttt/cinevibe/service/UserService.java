package com.ttt.cinevibe.service;

import com.ttt.cinevibe.model.User;

public interface UserService {
    User findByFirebaseUid(String firebaseUid);
    User createOrUpdateUser(String firebaseUid, String email, String displayName, String profileImageUrl);
    void updateLastLogin(String firebaseUid);
}