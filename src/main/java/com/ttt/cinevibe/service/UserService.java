package com.ttt.cinevibe.service;

import com.ttt.cinevibe.dto.request.UserProfileRequest;
import com.ttt.cinevibe.dto.request.UserRegisterRequest;
import com.ttt.cinevibe.dto.response.UserResponse;

public interface UserService {
    UserResponse currentUser(String firebaseUid);

    UserResponse syncUser(UserRegisterRequest userRegisterRequest);

    UserResponse updateUserProfile(UserProfileRequest profileRequest);

    void updateLastLogin(String firebaseUid);
}