package com.ttt.cinevibe.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@lombok.Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String firebaseUid;
    private String displayName;
    private String email;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
