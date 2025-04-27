package com.ttt.cinevibe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"watchlist_id", "tmdb_movie_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "watchlist_id", nullable = false)
    private Watchlist watchlist;
    
    @Column(name = "tmdb_movie_id", nullable = false)
    private Long tmdbMovieId;
    
    @Column(name = "movie_title", nullable = false)
    private String movieTitle;
    
    @Column(name = "poster_path")
    private String posterPath;
    
    @Column(name = "added_notes")
    private String addedNotes;
    
    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;
}