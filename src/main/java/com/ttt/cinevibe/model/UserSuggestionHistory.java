package com.ttt.cinevibe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_suggestion_history", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "suggested_user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSuggestionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "suggested_user_id", nullable = false)
    private User suggestedUser;
    
    @Column(name = "reason", length = 255)
    private String reason;
    
    @Column(name = "score")
    private Double score;
    
    @Column(name = "was_followed")
    private Boolean wasFollowed = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}