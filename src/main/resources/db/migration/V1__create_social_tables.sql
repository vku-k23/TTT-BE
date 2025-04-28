-- User table update (if it exists) or create (if it doesn't)
CREATE TABLE IF NOT EXISTS `users` (
    `firebase_uid` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `display_name` VARCHAR(100) NOT NULL,
    `profile_image_url` VARCHAR(255),
    `last_login` TIMESTAMP,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`firebase_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Watchlist table for user's movie collections
CREATE TABLE IF NOT EXISTS `watchlists` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` VARCHAR(100) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `is_public` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE,
    INDEX `idx_watchlist_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Watchlist items (movies in watchlists)
CREATE TABLE IF NOT EXISTS `watchlist_items` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `watchlist_id` BIGINT NOT NULL,
    `tmdb_movie_id` BIGINT NOT NULL,
    `movie_title` VARCHAR(255) NOT NULL,
    `poster_path` VARCHAR(255),
    `added_notes` TEXT,
    `added_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`watchlist_id`) REFERENCES `watchlists` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_watchlist_movie` (`watchlist_id`, `tmdb_movie_id`),
    INDEX `idx_tmdb_movie_id` (`tmdb_movie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Movie reviews
CREATE TABLE IF NOT EXISTS `movie_reviews` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` VARCHAR(100) NOT NULL,
    `tmdb_movie_id` BIGINT NOT NULL,
    `movie_title` VARCHAR(255) NOT NULL,
    `review_text` TEXT NOT NULL,
    `rating` FLOAT NOT NULL,
    `contains_spoilers` BOOLEAN NOT NULL DEFAULT FALSE,
    `likes_count` INT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_movie_review` (`user_id`, `tmdb_movie_id`),
    INDEX `idx_movie_review` (`tmdb_movie_id`),
    INDEX `idx_user_review` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comments on reviews
CREATE TABLE IF NOT EXISTS `comments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `review_id` BIGINT NOT NULL,
    `user_id` VARCHAR(100) NOT NULL,
    `content` TEXT NOT NULL,
    `likes_count` INT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`review_id`) REFERENCES `movie_reviews` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE,
    INDEX `idx_review_comments` (`review_id`),
    INDEX `idx_user_comments` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User connections (follows/friends)
CREATE TABLE IF NOT EXISTS `user_connections` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `follower_id` VARCHAR(100) NOT NULL,
    `following_id` VARCHAR(100) NOT NULL,
    `status` ENUM('PENDING', 'ACCEPTED', 'BLOCKED') NOT NULL DEFAULT 'PENDING',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`follower_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE,
    FOREIGN KEY (`following_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE,
    UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
    INDEX `idx_follower` (`follower_id`),
    INDEX `idx_following` (`following_id`),
    INDEX `idx_connection_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;