package com.ttt.cinevibe.service;

import com.ttt.cinevibe.dto.request.UserConnectionRequest;
import com.ttt.cinevibe.dto.response.UserConnectionResponse;
import com.ttt.cinevibe.model.UserConnection.ConnectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserConnectionService {
    
    Page<UserConnectionResponse> getFollowing(String userUid, Pageable pageable);
    
    Page<UserConnectionResponse> getFollowers(String userUid, Pageable pageable);
    
    Page<UserConnectionResponse> getPendingRequests(String userUid, Pageable pageable);
    
    UserConnectionResponse followUser(String userUid, UserConnectionRequest request);
    
    UserConnectionResponse acceptFollowRequest(String userUid, Long connectionId);
    
    void rejectFollowRequest(String userUid, Long connectionId);
    
    void unfollowUser(String userUid, String targetUserUid);
    
    void removeFollower(String userUid, String followerUid);
    
    boolean isFollowing(String userUid, String targetUserUid);
    
    ConnectionStatus getConnectionStatus(String userUid, String targetUserUid);

    void cancelFollowRequest(String userUid, String targetUserUid);
}