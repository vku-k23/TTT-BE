-- Insert sample users
INSERT IGNORE INTO users (firebase_uid, email, display_name, profile_image_url, last_login, created_at) VALUES
('sample-uid-1', 'user1@example.com', 'Movie Lover', 'https://ui-avatars.com/api/?name=Movie+Lover', NOW(), NOW()),
('sample-uid-2', 'user2@example.com', 'Film Critic', 'https://ui-avatars.com/api/?name=Film+Critic', NOW(), NOW()),
('sample-uid-3', 'user3@example.com', 'Cinema Fan', 'https://ui-avatars.com/api/?name=Cinema+Fan', NOW(), NOW());

-- Insert sample watchlists
INSERT INTO watchlists (user_id, name, description, is_public, created_at) VALUES
('sample-uid-1', 'My Favorites', 'The best movies I have ever seen', TRUE, NOW()),
('sample-uid-1', 'Watch Later', 'Movies I want to watch', FALSE, NOW()),
('sample-uid-2', 'Classic Films', 'The greatest classic movies of all time', TRUE, NOW()),
('sample-uid-3', 'Sci-Fi Collection', 'My favorite science fiction movies', TRUE, NOW());

-- Insert sample watchlist items (using actual TMDB movie IDs)
INSERT INTO watchlist_items (watchlist_id, tmdb_movie_id, movie_title, poster_path, added_notes) VALUES
(1, 299536, 'Avengers: Infinity War', '/7WsyChQLEftFiDOVTGkv3hFpyyt.jpg', 'Amazing movie with great action scenes'),
(1, 299534, 'Avengers: Endgame', '/or06FN3Dka5tukK1e9sl16pB3iy.jpg', 'Epic conclusion to the saga'),
(1, 550, 'Fight Club', '/8kNruSfhk5IoE4eZOc4UpvXJvpL.jpg', 'Mind-blowing twist at the end'),
(2, 157336, 'Interstellar', '/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg', 'Need to watch this soon'),
(3, 238, 'The Godfather', '/3bhkrj58Vtu7enYsRolD1fZdja1.jpg', 'One of the greatest films ever made'),
(3, 240, 'The Godfather: Part II', '/hek3koDUyRQk7FIhPXsa6mT2Zc3.jpg', 'Even better than the first one'),
(4, 335984, 'Blade Runner 2049', '/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg', 'Beautiful cinematography'),
(4, 78, 'Blade Runner', '/63N9uy8nd9j7Eog2axPQ8lbr3Wj.jpg', 'The original classic');

-- Insert sample movie reviews
INSERT INTO movie_reviews (user_id, tmdb_movie_id, movie_title, review_text, rating, contains_spoilers, likes_count, created_at) VALUES
('sample-uid-1', 299536, 'Avengers: Infinity War', 'This movie blew my mind with its massive scale and unexpected ending. The way they balanced so many characters was impressive.', 4.5, FALSE, 0, NOW()),
('sample-uid-2', 299536, 'Avengers: Infinity War', 'While entertaining, the film struggles with pacing issues and relies too heavily on previous MCU knowledge.', 3.5, FALSE, 0, NOW()),
('sample-uid-3', 238, 'The Godfather', 'A cinematic masterpiece that defined a generation of filmmaking. Brando and Pacino deliver unforgettable performances.', 5.0, FALSE, 0, NOW()),
('sample-uid-2', 335984, 'Blade Runner 2049', 'Visually stunning with a thought-provoking story that expands on the original in meaningful ways.', 4.5, TRUE, 0, NOW());

-- Insert sample comments
INSERT INTO comments (review_id, user_id, content, likes_count, created_at) VALUES
(1, 'sample-uid-2', 'I agree about the ending - totally unexpected!', 0, NOW()),
(1, 'sample-uid-3', 'The CGI for Thanos was incredible too', 0, NOW()),
(3, 'sample-uid-1', 'I need to rewatch this classic. Your review convinced me!', 0, NOW()),
(4, 'sample-uid-3', 'The cinematography was amazing, especially the cityscape scenes', 0, NOW());

-- Insert sample user connections
INSERT INTO user_connections (follower_id, following_id, status, created_at) VALUES
('sample-uid-1', 'sample-uid-2', 'ACCEPTED', NOW()),
('sample-uid-3', 'sample-uid-1', 'ACCEPTED', NOW()),
('sample-uid-2', 'sample-uid-3', 'PENDING', NOW());

-- Insert sample review likes
INSERT INTO review_likes (review_id, user_id, created_at) VALUES
(1, 'sample-uid-2', NOW()),
(1, 'sample-uid-3', NOW()),
(3, 'sample-uid-1', NOW()),
(3, 'sample-uid-2', NOW()),
(4, 'sample-uid-1', NOW());

-- Insert sample comment likes
INSERT INTO comment_likes (comment_id, user_id, created_at) VALUES
(1, 'sample-uid-1', NOW()),
(2, 'sample-uid-1', NOW()),
(3, 'sample-uid-3', NOW());

-- Update likes count on reviews and comments (the triggers will handle this for new data, but we need to update our initial data)
UPDATE movie_reviews r
SET r.likes_count = (SELECT COUNT(*) FROM review_likes WHERE review_id = r.id)
WHERE r.id IN (1, 3, 4);

UPDATE comments c
SET c.likes_count = (SELECT COUNT(*) FROM comment_likes WHERE comment_id = c.id)
WHERE c.id IN (1, 2, 3);