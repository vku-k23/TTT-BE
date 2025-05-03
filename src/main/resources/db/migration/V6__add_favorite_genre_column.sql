-- Add favorite_genre column to the users table
ALTER TABLE `users` 
    ADD COLUMN `favorite_genre` VARCHAR(100) NULL AFTER `bio`;
