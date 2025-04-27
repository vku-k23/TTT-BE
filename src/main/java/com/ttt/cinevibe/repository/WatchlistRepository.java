package com.ttt.cinevibe.repository;

import com.ttt.cinevibe.model.User;
import com.ttt.cinevibe.model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    List<Watchlist> findByUser(User user);
    List<Watchlist> findByUserAndIsPublic(User user, boolean isPublic);
    List<Watchlist> findByIsPublicTrue();
    Optional<Watchlist> findByIdAndUser(Long id, User user);
}