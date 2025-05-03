-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.42 - MySQL Community Server - GPL
-- Server OS:                    Linux
-- HeidiSQL Version:             12.10.0.7000
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for cinevibe
CREATE DATABASE IF NOT EXISTS `cinevibe` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `cinevibe`;

-- Dumping structure for table cinevibe.comments
CREATE TABLE IF NOT EXISTS `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `review_id` bigint NOT NULL,
  `user_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `likes_count` int NOT NULL DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_review_comments` (`review_id`),
  KEY `idx_user_comments` (`user_id`),
  KEY `idx_comments_likes_count` (`likes_count` DESC),
  FULLTEXT KEY `ftx_comments_content` (`content`),
  CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`review_id`) REFERENCES `movie_reviews` (`id`) ON DELETE CASCADE,
  CONSTRAINT `comments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table cinevibe.comments: ~4 rows (approximately)
REPLACE INTO `comments` (`id`, `review_id`, `user_id`, `content`, `likes_count`, `created_at`, `updated_at`) VALUES
	(1, 1, 'sample-uid-2', 'I agree about the ending - totally unexpected!', 1, '2025-05-03 10:30:27', '2025-05-03 10:30:27'),
	(2, 1, 'sample-uid-3', 'The CGI for Thanos was incredible too', 1, '2025-05-03 10:30:27', '2025-05-03 10:30:27'),
	(3, 3, 'sample-uid-1', 'I need to rewatch this classic. Your review convinced me!', 1, '2025-05-03 10:30:27', '2025-05-03 10:30:27'),
	(4, 4, 'sample-uid-3', 'The cinematography was amazing, especially the cityscape scenes', 0, '2025-05-03 10:30:27', '2025-05-03 10:30:27');

-- Dumping structure for table cinevibe.comment_likes
CREATE TABLE IF NOT EXISTS `comment_likes` (
  `comment_id` bigint NOT NULL,
  `user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`,`user_id`),
  KEY `idx_user_comment_likes` (`user_id`),
  CONSTRAINT `comment_likes_ibfk_1` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `comment_likes_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table cinevibe.comment_likes: ~3 rows (approximately)
REPLACE INTO `comment_likes` (`comment_id`, `user_id`, `created_at`) VALUES
	(1, 'sample-uid-1', '2025-05-03 10:30:27'),
	(2, 'sample-uid-1', '2025-05-03 10:30:27'),
	(3, 'sample-uid-3', '2025-05-03 10:30:27');

-- Dumping structure for table cinevibe.flyway_schema_history
CREATE TABLE IF NOT EXISTS `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table cinevibe.flyway_schema_history: ~7 rows (approximately)
REPLACE INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES
	(1, '1', 'create social tables', 'SQL', 'V1__create_social_tables.sql', -1292992512, 'root', '2025-05-03 10:30:26', 280, 1),
	(2, '2', 'add likes tracking', 'SQL', 'V2__add_likes_tracking.sql', -980688487, 'root', '2025-05-03 10:30:26', 118, 1),
	(3, '3', 'add additional indexes', 'SQL', 'V3__add_additional_indexes.sql', -1927162910, 'root', '2025-05-03 10:30:27', 979, 1),
	(4, '4', 'add sample data', 'SQL', 'V4__add_sample_data.sql', -923858801, 'root', '2025-05-03 10:30:27', 29, 1),
	(5, '5', 'update user model', 'SQL', 'V5__update_user_model.sql', 1054665441, 'root', '2025-05-03 10:30:28', 762, 1),
	(6, '6', 'add favorite genre column', 'SQL', 'V6__add_favorite_genre_column.sql', 1966415221, 'root', '2025-05-03 10:30:28', 27, 1),
	(7, '7', 'add social count columns', 'SQL', 'V7__add_social_count_columns.sql', 2067007343, 'root', '2025-05-03 10:30:28', 28, 1);

-- Dumping structure for table cinevibe.movie_reviews
CREATE TABLE IF NOT EXISTS `movie_reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tmdb_movie_id` bigint NOT NULL,
  `movie_title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `poster_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `review_text` varchar(5000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rating` float NOT NULL,
  `contains_spoilers` tinyint(1) NOT NULL DEFAULT '0',
  `visibility` enum('PUBLIC','FRIENDS','PRIVATE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PUBLIC',
  `likes_count` int NOT NULL DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_movie_review` (`user_id`,`tmdb_movie_id`),
  UNIQUE KEY `UK4fjwdaegu28fdxttfx7v9a65u` (`user_id`,`tmdb_movie_id`),
  KEY `idx_movie_review` (`tmdb_movie_id`),
  KEY `idx_user_review` (`user_id`),
  KEY `idx_movie_reviews_created_at` (`created_at` DESC),
  KEY `idx_movie_reviews_likes_count` (`likes_count` DESC),
  KEY `idx_movie_reviews_by_movie_popularity` (`tmdb_movie_id`,`likes_count` DESC),
  KEY `idx_movie_reviews_by_user_recent` (`user_id`,`created_at` DESC),
  FULLTEXT KEY `ftx_movie_reviews_content` (`review_text`),
  CONSTRAINT `movie_reviews_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table cinevibe.movie_reviews: ~4 rows (approximately)
REPLACE INTO `movie_reviews` (`id`, `user_id`, `tmdb_movie_id`, `movie_title`, `poster_path`, `review_text`, `rating`, `contains_spoilers`, `visibility`, `likes_count`, `created_at`, `updated_at`) VALUES
	(1, 'sample-uid-1', 299536, 'Avengers: Infinity War', NULL, 'This movie blew my mind with its massive scale and unexpected ending. The way they balanced so many characters was impressive.', 4.5, 0, 'PUBLIC', 2, '2025-05-03 10:30:27', '2025-05-03 10:30:27'),
	(2, 'sample-uid-2', 299536, 'Avengers: Infinity War', NULL, 'While entertaining, the film struggles with pacing issues and relies too heavily on previous MCU knowledge.', 3.5, 0, 'PUBLIC', 0, '2025-05-03 10:30:27', '2025-05-03 10:30:27'),
	(3, 'sample-uid-3', 238, 'The Godfather', NULL, 'A cinematic masterpiece that defined a generation of filmmaking. Brando and Pacino deliver unforgettable performances.', 5, 0, 'PUBLIC', 2, '2025-05-03 10:30:27', '2025-05-03 10:30:27'),
	(4, 'sample-uid-2', 335984, 'Blade Runner 2049', NULL, 'Visually stunning with a thought-provoking story that expands on the original in meaningful ways.', 4.5, 1, 'PUBLIC', 1, '2025-05-03 10:30:27', '2025-05-03 10:30:27');

-- Dumping structure for table cinevibe.review_likes
CREATE TABLE IF NOT EXISTS `review_likes` (
  `review_id` bigint NOT NULL,
  `user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`review_id`,`user_id`),
  KEY `idx_user_review_likes` (`user_id`),
  CONSTRAINT `review_likes_ibfk_1` FOREIGN KEY (`review_id`) REFERENCES `movie_reviews` (`id`) ON DELETE CASCADE,
  CONSTRAINT `review_likes_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table cinevibe.review_likes: ~5 rows (approximately)
REPLACE INTO `review_likes` (`review_id`, `user_id`, `created_at`) VALUES
	(1, 'sample-uid-2', '2025-05-03 10:30:27'),
	(1, 'sample-uid-3', '2025-05-03 10:30:27'),
	(3, 'sample-uid-1', '2025-05-03 10:30:27'),
	(3, 'sample-uid-2', '2025-05-03 10:30:27'),
	(4, 'sample-uid-1', '2025-05-03 10:30:27');

-- Dumping structure for table cinevibe.saved_movies
CREATE TABLE IF NOT EXISTS `saved_movies` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tmdb_movie_id` bigint NOT NULL,
  `saved_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_saved_movie` (`user_id`,`tmdb_movie_id`),
  KEY `idx_user_saved` (`user_id`),
  CONSTRAINT `saved_movies_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table cinevibe.saved_movies: ~0 rows (approximately)

-- Dumping structure for table cinevibe.users
CREATE TABLE IF NOT EXISTS `users` (
  `firebase_uid` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `display_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `profile_image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bio` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `favorite_genre` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `followers_count` int NOT NULL DEFAULT '0',
  `following_count` int NOT NULL DEFAULT '0',
  `review_count` int NOT NULL DEFAULT '0',
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `preferences` json DEFAULT NULL,
  `is_verified` tinyint(1) NOT NULL DEFAULT '0',
  `last_login` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`firebase_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table cinevibe.users: ~5 rows (approximately)
REPLACE INTO `users` (`firebase_uid`, `email`, `display_name`, `profile_image_url`, `bio`, `favorite_genre`, `followers_count`, `following_count`, `review_count`, `location`, `preferences`, `is_verified`, `last_login`, `created_at`, `updated_at`) VALUES
	('12311', 'ok@gmail.com', 'ok test', NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 0, '2025-05-03 17:35:01', '2025-05-03 17:35:01', '2025-05-03 10:35:01'),
	('9jek6pO2usbmeC7dnPscO3x05Kk1', 'testw@gmail.com', 'testw', NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 0, '2025-05-03 17:48:53', '2025-05-03 17:48:53', '2025-05-03 10:48:53'),
	('Aqd6c5xmutcHcpMMtDnoN7NxaPO2', 'test@gmail.com', 'test', NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 0, '2025-05-03 22:55:12', '2025-05-03 22:55:12', '2025-05-03 15:55:12'),
	('J4q460PqDMT5qVuS510ICNci4x42', 'test44@gmail.com', 'test44', NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 0, '2025-05-03 22:56:34', '2025-05-03 22:56:34', '2025-05-03 15:56:34'),
	('okok', 'ok@gmail.com', 'ok test', NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 0, '2025-05-03 17:30:35', '2025-05-03 17:30:35', '2025-05-03 10:30:34'),
	('sample-uid-1', 'user1@example.com', 'Movie Lover', 'https://ui-avatars.com/api/?name=Movie+Lover', NULL, NULL, 0, 0, 0, NULL, NULL, 0, '2025-05-03 10:30:27', '2025-05-03 10:30:27', '2025-05-03 10:30:27'),
	('sample-uid-2', 'user2@example.com', 'Film Critic', 'https://ui-avatars.com/api/?name=Film+Critic', NULL, NULL, 0, 0, 0, NULL, NULL, 0, '2025-05-03 10:30:27', '2025-05-03 10:30:27', '2025-05-03 10:30:27'),
	('sample-uid-3', 'user3@example.com', 'Cinema Fan', 'https://ui-avatars.com/api/?name=Cinema+Fan', NULL, NULL, 0, 0, 0, NULL, NULL, 0, '2025-05-03 10:30:27', '2025-05-03 10:30:27', '2025-05-03 10:30:27');

-- Dumping structure for table cinevibe.user_connections
CREATE TABLE IF NOT EXISTS `user_connections` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `follower_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `following_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('PENDING','ACCEPTED','BLOCKED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_following` (`follower_id`,`following_id`),
  UNIQUE KEY `UKtjciatdbea0nd46rxfa8fx05c` (`follower_id`,`following_id`),
  KEY `idx_follower` (`follower_id`),
  KEY `idx_following` (`following_id`),
  KEY `idx_connection_status` (`status`),
  CONSTRAINT `user_connections_ibfk_1` FOREIGN KEY (`follower_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE,
  CONSTRAINT `user_connections_ibfk_2` FOREIGN KEY (`following_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table cinevibe.user_connections: ~4 rows (approximately)
REPLACE INTO `user_connections` (`id`, `follower_id`, `following_id`, `status`, `created_at`) VALUES
	(1, 'sample-uid-1', 'sample-uid-2', 'ACCEPTED', '2025-05-03 10:30:27'),
	(2, 'sample-uid-3', 'sample-uid-1', 'ACCEPTED', '2025-05-03 10:30:27'),
	(3, 'sample-uid-2', 'sample-uid-3', 'PENDING', '2025-05-03 10:30:27'),
	(4, 'J4q460PqDMT5qVuS510ICNci4x42', 'Aqd6c5xmutcHcpMMtDnoN7NxaPO2', 'ACCEPTED', '2025-05-03 23:21:02');

-- Dumping structure for table cinevibe.watchlists
CREATE TABLE IF NOT EXISTS `watchlists` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cover_image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_public` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_watchlist_user` (`user_id`),
  FULLTEXT KEY `ftx_watchlists_content` (`name`,`description`),
  CONSTRAINT `watchlists_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`firebase_uid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table cinevibe.watchlists: ~4 rows (approximately)
REPLACE INTO `watchlists` (`id`, `user_id`, `name`, `description`, `cover_image_url`, `is_public`, `created_at`, `updated_at`) VALUES
	(1, 'sample-uid-1', 'My Favorites', 'The best movies I have ever seen', NULL, 1, '2025-05-03 10:30:27', '2025-05-03 10:30:27'),
	(2, 'sample-uid-1', 'Watch Later', 'Movies I want to watch', NULL, 0, '2025-05-03 10:30:27', '2025-05-03 10:30:27'),
	(3, 'sample-uid-2', 'Classic Films', 'The greatest classic movies of all time', NULL, 1, '2025-05-03 10:30:27', '2025-05-03 10:30:27'),
	(4, 'sample-uid-3', 'Sci-Fi Collection', 'My favorite science fiction movies', NULL, 1, '2025-05-03 10:30:27', '2025-05-03 10:30:27');

-- Dumping structure for table cinevibe.watchlist_items
CREATE TABLE IF NOT EXISTS `watchlist_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `watchlist_id` bigint NOT NULL,
  `tmdb_movie_id` bigint NOT NULL,
  `movie_title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `poster_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `added_notes` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `added_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_watchlist_movie` (`watchlist_id`,`tmdb_movie_id`),
  UNIQUE KEY `UKommf1a1v1n3tmfnmpfalkewv9` (`watchlist_id`,`tmdb_movie_id`),
  KEY `idx_tmdb_movie_id` (`tmdb_movie_id`),
  CONSTRAINT `watchlist_items_ibfk_1` FOREIGN KEY (`watchlist_id`) REFERENCES `watchlists` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table cinevibe.watchlist_items: ~8 rows (approximately)
REPLACE INTO `watchlist_items` (`id`, `watchlist_id`, `tmdb_movie_id`, `movie_title`, `poster_path`, `added_notes`, `added_at`) VALUES
	(1, 1, 299536, 'Avengers: Infinity War', '/7WsyChQLEftFiDOVTGkv3hFpyyt.jpg', 'Amazing movie with great action scenes', '2025-05-03 10:30:27'),
	(2, 1, 299534, 'Avengers: Endgame', '/or06FN3Dka5tukK1e9sl16pB3iy.jpg', 'Epic conclusion to the saga', '2025-05-03 10:30:27'),
	(3, 1, 550, 'Fight Club', '/8kNruSfhk5IoE4eZOc4UpvXJvpL.jpg', 'Mind-blowing twist at the end', '2025-05-03 10:30:27'),
	(4, 2, 157336, 'Interstellar', '/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg', 'Need to watch this soon', '2025-05-03 10:30:27'),
	(5, 3, 238, 'The Godfather', '/3bhkrj58Vtu7enYsRolD1fZdja1.jpg', 'One of the greatest films ever made', '2025-05-03 10:30:27'),
	(6, 3, 240, 'The Godfather: Part II', '/hek3koDUyRQk7FIhPXsa6mT2Zc3.jpg', 'Even better than the first one', '2025-05-03 10:30:27'),
	(7, 4, 335984, 'Blade Runner 2049', '/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg', 'Beautiful cinematography', '2025-05-03 10:30:27'),
	(8, 4, 78, 'Blade Runner', '/63N9uy8nd9j7Eog2axPQ8lbr3Wj.jpg', 'The original classic', '2025-05-03 10:30:27');

-- Dumping structure for trigger cinevibe.update_comment_likes_count_delete
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `update_comment_likes_count_delete` AFTER DELETE ON `comment_likes` FOR EACH ROW BEGIN
    UPDATE comments
    SET likes_count = (
        SELECT COUNT(*) FROM comment_likes
        WHERE comment_id = OLD.comment_id
    )
    WHERE id = OLD.comment_id;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

-- Dumping structure for trigger cinevibe.update_comment_likes_count_insert
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `update_comment_likes_count_insert` AFTER INSERT ON `comment_likes` FOR EACH ROW BEGIN
    UPDATE comments
    SET likes_count = (
        SELECT COUNT(*) FROM comment_likes
        WHERE comment_id = NEW.comment_id
    )
    WHERE id = NEW.comment_id;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

-- Dumping structure for trigger cinevibe.update_review_likes_count_delete
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `update_review_likes_count_delete` AFTER DELETE ON `review_likes` FOR EACH ROW BEGIN
    UPDATE movie_reviews
    SET likes_count = (
        SELECT COUNT(*) FROM review_likes
        WHERE review_id = OLD.review_id
    )
    WHERE id = OLD.review_id;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

-- Dumping structure for trigger cinevibe.update_review_likes_count_insert
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `update_review_likes_count_insert` AFTER INSERT ON `review_likes` FOR EACH ROW BEGIN
    UPDATE movie_reviews
    SET likes_count = (
        SELECT COUNT(*) FROM review_likes
        WHERE review_id = NEW.review_id
    )
    WHERE id = NEW.review_id;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
