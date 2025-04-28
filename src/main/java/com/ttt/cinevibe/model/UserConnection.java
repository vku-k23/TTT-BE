package com.ttt.cinevibe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_connections", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"follower_id", "following_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;
    
    @ManyToOne
    @JoinColumn(name = "following_id", nullable = false)
    private User following;
    
    // Status: PENDING, ACCEPTED, BLOCKED
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConnectionStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum ConnectionStatus {
        PENDING,
        ACCEPTED,
        BLOCKED
    }
}