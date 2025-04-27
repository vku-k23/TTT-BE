package com.ttt.cinevibe.repository;

import com.ttt.cinevibe.model.Watchlist;
import com.ttt.cinevibe.model.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {
    List<WatchlistItem> findByWatchlist(Watchlist watchlist);
    List<WatchlistItem> findByWatchlistIn(List<Watchlist> watchlists);
    Optional<WatchlistItem> findByWatchlistAndTmdbMovieId(Watchlist watchlist, Long tmdbMovieId);
    void deleteByWatchlistAndTmdbMovieId(Watchlist watchlist, Long tmdbMovieId);
    boolean existsByWatchlistAndTmdbMovieId(Watchlist watchlist, Long tmdbMovieId);
    long countByWatchlist(Watchlist watchlist);
}