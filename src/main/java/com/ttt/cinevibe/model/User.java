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
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
}