package com.ttt.cinevibe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "firebase_uid", length = 128)
    private String firebaseUid;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    
    @Column(name = "bio", length = 500)
    private String bio;
    
    @Column(name = "favorite_genre")
    private String favoriteGenre;
    
    @Column(name = "review_count")
    private Integer reviewCount = 0;
    
    @Column(name = "followers_count")
    private Integer followersCount = 0;
    
    @Column(name = "following_count")
    private Integer followingCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
}