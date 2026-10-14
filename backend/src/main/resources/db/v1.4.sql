-- v1.4 社交与分享
-- 执行方式：mysql -u root -p media_website < v1.4.sql

-- 媒体点赞（公开媒体可被任意登录用户点赞，一人一赞）
CREATE TABLE IF NOT EXISTS media_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    media_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_media_user (media_id, user_id)
);

-- 相册分享令牌（null = 未开启分享）
ALTER TABLE albums ADD COLUMN share_token VARCHAR(64) DEFAULT NULL;
