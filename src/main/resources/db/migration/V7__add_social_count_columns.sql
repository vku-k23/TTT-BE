-- Add social count columns to the users table
ALTER TABLE `users` 
    ADD COLUMN `followers_count` INT NOT NULL DEFAULT 0 AFTER `favorite_genre`,
    ADD COLUMN `following_count` INT NOT NULL DEFAULT 0 AFTER `followers_count`,
    ADD COLUMN `review_count` INT NOT NULL DEFAULT 0 AFTER `following_count`;
