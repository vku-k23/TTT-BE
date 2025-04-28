package com.ttt.cinevibe.service;

import com.ttt.cinevibe.dto.request.UserRequest;
import com.ttt.cinevibe.dto.response.UserResponse;

public interface UserService {
    UserResponse findByFirebaseUid(String firebaseUid);
    UserResponse createOrUpdateUser(UserRequest userRequest);
    void updateLastLogin(String firebaseUid);
}