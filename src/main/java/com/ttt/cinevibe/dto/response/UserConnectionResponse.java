package com.ttt.cinevibe.dto.response;

import com.ttt.cinevibe.model.UserConnection.ConnectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConnectionResponse {
    private Long id;
    private String followerUid;
    private String followerName;
    private String followerProfileImageUrl;
    private String followingUid;
    private String followingName;
    private String followingProfileImageUrl;
    private ConnectionStatus status;
    private LocalDateTime createdAt;
}