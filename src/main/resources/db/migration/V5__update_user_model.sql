-- User table updates
ALTER TABLE `users` 
    ADD COLUMN `bio` TEXT AFTER `profile_image_url`,
    ADD COLUMN `location` VARCHAR(100) AFTER `bio`,
    ADD COLUMN `preferences` JSON AFTER `location`,
    ADD COLUMN `is_verified` BOOLEAN NOT NULL DEFAULT FALSE AFTER `preferences`;

-- Movie reviews updates
ALTER TABLE `movie_reviews` 
    ADD COLUMN `poster_path` VARCHAR(255) AFTER `movie_title`,
    ADD COLUMN `visibility` ENUM('PUBLIC', 'FRIENDS', 'PRIVATE') NOT NULL DEFAULT 'PUBLIC' AFTER `contains_spoilers`;

-- Watchlists updates
ALTER TABLE `watchlists`
    MODIFY COLUMN `description` VARCHAR(500),
    ADD COLUMN `cover_image_url` VARCHAR(255) AFTER `description`;

-- Add new table for user saved movies (not in watchlists)
CREATE TABLE IF NOT EXISTS `saved_movies` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` VARCHAR(100) NOT NULL,
    `tmdb_movie_id` BIGINT NOT NULL,
    `saved_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_saved_movie` (`user_id`, `tmdb_movie_id`),
    INDEX `idx_user_saved` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
