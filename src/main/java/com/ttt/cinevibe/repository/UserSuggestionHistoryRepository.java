package com.ttt.cinevibe.repository;

import com.ttt.cinevibe.model.User;
import com.ttt.cinevibe.model.UserSuggestionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UserSuggestionHistoryRepository extends JpaRepository<UserSuggestionHistory, Long> {
    
    List<UserSuggestionHistory> findByUserOrderByScoreDesc(User user);
    
    List<UserSuggestionHistory> findByUserAndCreatedAtAfterOrderByScoreDesc(User user, LocalDateTime dateTime);
    
    @Query("SELECT ush.suggestedUser.firebaseUid FROM UserSuggestionHistory ush WHERE ush.user = :user")
    List<String> findSuggestedUserIdsByUser(User user);
    
    boolean existsByUserAndSuggestedUser(User user, User suggestedUser);
}