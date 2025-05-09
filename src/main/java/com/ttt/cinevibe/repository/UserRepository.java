package com.ttt.cinevibe.repository;

import com.ttt.cinevibe.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchByUsernameOrDisplayName(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchByUsernameOrDisplayName(@Param("searchTerm") String searchTerm, Pageable pageable);
}