CREATE TABLE IF NOT EXISTS user_suggestion_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    suggested_user_id VARCHAR(128) NOT NULL,
    reason VARCHAR(255),
    score DOUBLE,
    was_followed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_suggestion_user FOREIGN KEY (user_id) 
    REFERENCES users (firebase_uid) ON DELETE CASCADE,
    
    CONSTRAINT fk_user_suggestion_suggested FOREIGN KEY (suggested_user_id) 
    REFERENCES users (firebase_uid) ON DELETE CASCADE,
    
    CONSTRAINT uk_user_suggestion UNIQUE (user_id, suggested_user_id)
);

CREATE INDEX idx_user_suggestion_user ON user_suggestion_history (user_id);
CREATE INDEX idx_user_suggestion_suggested ON user_suggestion_history (suggested_user_id);
CREATE INDEX idx_user_suggestion_created_at ON user_suggestion_history (created_at);