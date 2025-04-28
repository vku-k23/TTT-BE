-- Add additional indexes for common queries to improve performance

-- Index for finding recent reviews
CREATE INDEX idx_movie_reviews_created_at ON movie_reviews(created_at DESC);

-- Index for finding popular reviews
CREATE INDEX idx_movie_reviews_likes_count ON movie_reviews(likes_count DESC);

-- Index for finding popular comments
CREATE INDEX idx_comments_likes_count ON comments(likes_count DESC);

-- Combined index for finding reviews by movie and sorting by popularity
CREATE INDEX idx_movie_reviews_by_movie_popularity ON movie_reviews(tmdb_movie_id, likes_count DESC);

-- Combined index for finding reviews by user and sorting by recency
CREATE INDEX idx_movie_reviews_by_user_recent ON movie_reviews(user_id, created_at DESC);

-- Add full-text search capability to reviews for searching by content
ALTER TABLE movie_reviews ADD FULLTEXT INDEX ftx_movie_reviews_content(review_text);

-- Add full-text search for comments
ALTER TABLE comments ADD FULLTEXT INDEX ftx_comments_content(content);

-- Add full-text search for watchlists and descriptions
ALTER TABLE watchlists ADD FULLTEXT INDEX ftx_watchlists_content(name, description);