-- v1.3 用户头像体系
-- 执行方式：mysql -u root -p media_website < v1.3.sql

-- users 表建表语句补齐（此前仓库缺失，仅线上库存在）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 头像与头像背景
ALTER TABLE users ADD COLUMN avatar VARCHAR(512) DEFAULT NULL;
ALTER TABLE users ADD COLUMN avatar_bg VARCHAR(512) DEFAULT NULL;
