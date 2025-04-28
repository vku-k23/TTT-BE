-- Review likes (to track which users liked which reviews)
CREATE TABLE IF NOT EXISTS `review_likes` (
    `review_id` BIGINT NOT NULL,
    `user_id` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`review_id`, `user_id`),
    FOREIGN KEY (`review_id`) REFERENCES `movie_reviews` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE,
    INDEX `idx_user_review_likes` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comment likes (to track which users liked which comments)
CREATE TABLE IF NOT EXISTS `comment_likes` (
    `comment_id` BIGINT NOT NULL,
    `user_id` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`comment_id`, `user_id`),
    FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE,
    INDEX `idx_user_comment_likes` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add triggers to automatically update likes count on movie_reviews table
DELIMITER //

CREATE TRIGGER update_review_likes_count_insert AFTER INSERT ON review_likes
FOR EACH ROW
BEGIN
    UPDATE movie_reviews
    SET likes_count = (
        SELECT COUNT(*) FROM review_likes
        WHERE review_id = NEW.review_id
    )
    WHERE id = NEW.review_id;
END//

CREATE TRIGGER update_review_likes_count_delete AFTER DELETE ON review_likes
FOR EACH ROW
BEGIN
    UPDATE movie_reviews
    SET likes_count = (
        SELECT COUNT(*) FROM review_likes
        WHERE review_id = OLD.review_id
    )
    WHERE id = OLD.review_id;
END//

-- Add triggers to automatically update likes count on comments table
CREATE TRIGGER update_comment_likes_count_insert AFTER INSERT ON comment_likes
FOR EACH ROW
BEGIN
    UPDATE comments
    SET likes_count = (
        SELECT COUNT(*) FROM comment_likes
        WHERE comment_id = NEW.comment_id
    )
    WHERE id = NEW.comment_id;
END//

CREATE TRIGGER update_comment_likes_count_delete AFTER DELETE ON comment_likes
FOR EACH ROW
BEGIN
    UPDATE comments
    SET likes_count = (
        SELECT COUNT(*) FROM comment_likes
        WHERE comment_id = OLD.comment_id
    )
    WHERE id = OLD.comment_id;
END//

DELIMITER ;