package com.ttt.cinevibe.service.impl;

import com.ttt.cinevibe.dto.request.UserConnectionRequest;
import com.ttt.cinevibe.dto.response.UserConnectionResponse;
import com.ttt.cinevibe.dto.response.UserResponse;
import com.ttt.cinevibe.exception.DuplicateResourceException;
import com.ttt.cinevibe.exception.ResourceNotFoundException;
import com.ttt.cinevibe.exception.UnauthorizedException;
import com.ttt.cinevibe.model.User;
import com.ttt.cinevibe.model.UserConnection;
import com.ttt.cinevibe.model.UserConnection.ConnectionStatus;
import com.ttt.cinevibe.repository.UserConnectionRepository;
import com.ttt.cinevibe.repository.UserRepository;
import com.ttt.cinevibe.service.UserConnectionService;
import com.ttt.cinevibe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserConnectionServiceImpl implements UserConnectionService {

    private final UserConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public Page<UserConnectionResponse> getFollowing(String userUid, Pageable pageable) {
        User user = getUserOrThrow(userUid);
        return connectionRepository.findByFollowerAndStatus(user, ConnectionStatus.ACCEPTED, pageable)
                .map(this::mapToUserConnectionResponse);
    }
    
    @Override
    public Page<UserConnectionResponse> getUserFollowing(String targetUserUid, Pageable pageable) {
        User user = getUserOrThrow(targetUserUid);
        return connectionRepository.findByFollowerAndStatus(user, ConnectionStatus.ACCEPTED, pageable)
                .map(this::mapToUserConnectionResponse);
    }

    @Override
    public Page<UserConnectionResponse> getFollowers(String userUid, Pageable pageable) {
        User user = getUserOrThrow(userUid);
        return connectionRepository.findByFollowingAndStatus(user, ConnectionStatus.ACCEPTED, pageable)
                .map(this::mapToUserConnectionResponse);
    }
    
    @Override
    public Page<UserConnectionResponse> getUserFollowers(String targetUserUid, Pageable pageable) {
        User user = getUserOrThrow(targetUserUid);
        return connectionRepository.findByFollowingAndStatus(user, ConnectionStatus.ACCEPTED, pageable)
                .map(this::mapToUserConnectionResponse);
    }

    @Override
    public Page<UserConnectionResponse> getPendingRequests(String userUid, Pageable pageable) {
        User user = getUserOrThrow(userUid);
        return connectionRepository.findByFollowingAndStatus(user, ConnectionStatus.PENDING, pageable)
                .map(this::mapToUserConnectionResponse);
    }

    @Override
    @Transactional
    public UserConnectionResponse followUser(String userUid, UserConnectionRequest request) {
        User follower = getUserOrThrow(userUid);
        User following = getUserOrThrow(request.getTargetUserUid());
        
        // Prevent self-follow
        if (follower.getFirebaseUid().equals(following.getFirebaseUid())) {
            throw new DuplicateResourceException("You cannot follow yourself");
        }
        
        // Check if connection already exists
        Optional<UserConnection> existingConnection = 
                connectionRepository.findByFollowerAndFollowing(follower, following);
        
        if (existingConnection.isPresent()) {
            UserConnection connection = existingConnection.get();
            
            // If connection is already ACCEPTED or PENDING, return it
            if (connection.getStatus() == ConnectionStatus.ACCEPTED ||
                connection.getStatus() == ConnectionStatus.PENDING) {
                return mapToUserConnectionResponse(connection);
            }
            
            // If connection was BLOCKED, update to PENDING
            if (connection.getStatus() == ConnectionStatus.BLOCKED) {
                connection.setStatus(ConnectionStatus.PENDING);
                connection = connectionRepository.save(connection);
                log.info("Updated blocked connection to pending: {} -> {}", 
                       follower.getFirebaseUid(), following.getFirebaseUid());
                return mapToUserConnectionResponse(connection);
            }
        }
        
        // Create new connection with PENDING status
        UserConnection connection = UserConnection.builder()
                .follower(follower)
                .following(following)
                .status(ConnectionStatus.PENDING)
                .build();
        
        connection = connectionRepository.save(connection);
        log.info("Created new follow request: {} -> {}", 
                follower.getFirebaseUid(), following.getFirebaseUid());
        
        return mapToUserConnectionResponse(connection);
    }

    @Override
    @Transactional
    public UserConnectionResponse acceptFollowRequest(String userUid, Long connectionId) {
        User user = getUserOrThrow(userUid);
        
        UserConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with ID: " + connectionId));
        
        // Verify this is a request to follow the current user
        if (!connection.getFollowing().getFirebaseUid().equals(userUid)) {
            throw new UnauthorizedException("You don't have permission to accept this request");
        }
        
        // Verify request is in PENDING state
        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("This request is not in a pending state");
        }
        
        connection.setStatus(ConnectionStatus.ACCEPTED);
        connection = connectionRepository.save(connection);
        
        // Update follower and following counts
        User follower = connection.getFollower();
        User following = connection.getFollowing();
                
        // Increment follower's following count
        follower.setFollowingCount(follower.getFollowingCount() + 1);
        userRepository.save(follower);
                
        // Increment target user's followers count
        following.setFollowersCount(following.getFollowersCount() + 1);
        userRepository.save(following);
        
        log.info("Accepted follow request: {} -> {}, updated follower counts", 
                connection.getFollower().getFirebaseUid(), userUid);
        
        return mapToUserConnectionResponse(connection);
    }

    @Override
    @Transactional
    public void rejectFollowRequest(String userUid, Long connectionId) {
        User user = getUserOrThrow(userUid);
        
        UserConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with ID: " + connectionId));
        
        // Verify this is a request to follow the current user
        if (!connection.getFollowing().getFirebaseUid().equals(userUid)) {
            throw new UnauthorizedException("You don't have permission to reject this request");
        }
        
        // Verify request is in PENDING state
        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("This request is not in a pending state");
        }
        
        connectionRepository.delete(connection);
        
        log.info("Rejected follow request: {} -> {}", 
                connection.getFollower().getFirebaseUid(), userUid);
    }

    @Override
    @Transactional
    public void unfollowUser(String userUid, String targetUserUid) {
        User follower = getUserOrThrow(userUid);
        User following = getUserOrThrow(targetUserUid);
        
        Optional<UserConnection> existingConnection = 
                connectionRepository.findByFollowerAndFollowing(follower, following);
        
        if (existingConnection.isPresent()) {
            UserConnection connection = existingConnection.get();
            
            // Only decrease counts if the connection status was ACCEPTED
            if (connection.getStatus() == ConnectionStatus.ACCEPTED) {
                // Initialize followingCount to 0 if it's null
                follower.setFollowingCount(follower.getFollowingCount() == null ? 0 : follower.getFollowingCount());
                
                // Then decrement if greater than 0
                Integer followingCount = follower.getFollowingCount();
                if (followingCount > 0) {
                    follower.setFollowingCount(followingCount - 1);
                    userRepository.save(follower);
                }
                
                // Initialize followersCount to 0 if it's null
                following.setFollowersCount(following.getFollowersCount() == null ? 0 : following.getFollowersCount());
                
                // Then decrement if greater than 0
                Integer followersCount = following.getFollowersCount();
                if (followersCount > 0) {
                    following.setFollowersCount(followersCount - 1);
                    userRepository.save(following);
                }
                
                log.info("Updated follower/following counts after unfollow: {} -> {}", 
                        userUid, targetUserUid);
            }
            
            connectionRepository.delete(connection);
            log.info("Removed follow connection: {} -> {}", userUid, targetUserUid);
        }
    }

    @Override
    @Transactional
    public void removeFollower(String userUid, String followerUid) {
        User user = getUserOrThrow(userUid);
        User follower = getUserOrThrow(followerUid);
        
        Optional<UserConnection> existingConnection = 
                connectionRepository.findByFollowerAndFollowing(follower, user);
        
        if (existingConnection.isPresent()) {
            UserConnection connection = existingConnection.get();
            
            // Only decrease counts if the connection status was ACCEPTED
            if (connection.getStatus() == ConnectionStatus.ACCEPTED) {
                // Initialize followingCount to 0 if it's null
                follower.setFollowingCount(follower.getFollowingCount() == null ? 0 : follower.getFollowingCount());
                
                // Then decrement if greater than 0
                Integer followingCount = follower.getFollowingCount();
                if (followingCount > 0) {
                    follower.setFollowingCount(followingCount - 1);
                    userRepository.save(follower);
                }
                
                // Initialize followersCount to 0 if it's null
                user.setFollowersCount(user.getFollowersCount() == null ? 0 : user.getFollowersCount());
                
                // Then decrement if greater than 0
                Integer followersCount = user.getFollowersCount();
                if (followersCount > 0) {
                    user.setFollowersCount(followersCount - 1);
                    userRepository.save(user);
                }
                
                log.info("Updated follower/following counts after removing follower: {} -> {}", 
                        followerUid, userUid);
            }
            
            connectionRepository.delete(connection);
            log.info("Removed follower: {} -> {}", followerUid, userUid);
        }
    }

    @Override
    public boolean isFollowing(String userUid, String targetUserUid) {
        try {
            User follower = getUserOrThrow(userUid);
            User following = getUserOrThrow(targetUserUid);
            
            Optional<UserConnection> connection = 
                    connectionRepository.findByFollowerAndFollowing(follower, following);
            
            return connection.isPresent() && connection.get().getStatus() == ConnectionStatus.ACCEPTED;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    @Override
    public ConnectionStatus getConnectionStatus(String userUid, String targetUserUid) {
        try {
            User follower = getUserOrThrow(userUid);
            User following = getUserOrThrow(targetUserUid);
            
            Optional<UserConnection> connection = 
                    connectionRepository.findByFollowerAndFollowing(follower, following);
            
            return connection.map(UserConnection::getStatus).orElse(null);
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }
    
    @Override
    @Transactional
    public void cancelFollowRequest(String userUid, String targetUserUid) {
        User follower = getUserOrThrow(userUid);
        User following = getUserOrThrow(targetUserUid);
        
        Optional<UserConnection> existingConnection = 
                connectionRepository.findByFollowerAndFollowing(follower, following);
        
        if (existingConnection.isPresent()) {
            UserConnection connection = existingConnection.get();
            
            // Verify request is in PENDING state
            if (connection.getStatus() != ConnectionStatus.PENDING) {
                throw new IllegalStateException("This request is not in a pending state");
            }
            
            // Verify the current user is the one who sent the request
            if (!connection.getFollower().getFirebaseUid().equals(userUid)) {
                throw new UnauthorizedException("You don't have permission to cancel this request");
            }
            
            connectionRepository.delete(connection);
            
            log.info("Cancelled follow request: {} -> {}", userUid, targetUserUid);
        } else {
            throw new ResourceNotFoundException("Follow request not found");
        }
    }

    private User getUserOrThrow(String userUid) {
        UserResponse user = userService.currentUser(userUid);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with UID: " + userUid);
        }
        return mapToUser(user);
    }
    
    private UserConnectionResponse mapToUserConnectionResponse(UserConnection connection) {
        return UserConnectionResponse.builder()
                .id(connection.getId())
                .followerUid(connection.getFollower().getFirebaseUid())
                .followerName(connection.getFollower().getDisplayName())
                .followerProfileImageUrl(connection.getFollower().getProfileImageUrl())
                .followingUid(connection.getFollowing().getFirebaseUid())
                .followingName(connection.getFollowing().getDisplayName())
                .followingProfileImageUrl(connection.getFollowing().getProfileImageUrl())
                .status(connection.getStatus())
                .createdAt(connection.getCreatedAt())
                .build();
    }

    private User mapToUser(UserResponse userResponse) {
        return User.builder()
                .firebaseUid(userResponse.getFirebaseUid())
                .email(userResponse.getEmail())
                .displayName(userResponse.getDisplayName())
                .profileImageUrl(userResponse.getProfileImageUrl())
                .createdAt(userResponse.getCreatedAt())
                .lastLogin(userResponse.getLastLogin())
                .build();
    }
}