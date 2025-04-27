package com.ttt.cinevibe.repository;

import com.ttt.cinevibe.model.User;
import com.ttt.cinevibe.model.UserConnection;
import com.ttt.cinevibe.model.UserConnection.ConnectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {
    List<UserConnection> findByFollowerAndStatus(User follower, ConnectionStatus status);
    List<UserConnection> findByFollowingAndStatus(User following, ConnectionStatus status);
    
    Page<UserConnection> findByFollowerAndStatus(User follower, ConnectionStatus status, Pageable pageable);
    Page<UserConnection> findByFollowingAndStatus(User following, ConnectionStatus status, Pageable pageable);
    
    Optional<UserConnection> findByFollowerAndFollowing(User follower, User following);
    boolean existsByFollowerAndFollowing(User follower, User following);
}