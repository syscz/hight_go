-- MySQL数据库表结构设计

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS media_website CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE media_website;

-- 媒体表
CREATE TABLE IF NOT EXISTS media (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    media_type ENUM('image', 'video') NOT NULL,
    title VARCHAR(255),
    description TEXT,
    category_id BIGINT,
    cover_image VARCHAR(512),
    view_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 分类表
CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 标签表
CREATE TABLE IF NOT EXISTS tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 媒体标签关联表
CREATE TABLE IF NOT EXISTS media_tag (
    media_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (media_id, tag_id),
    FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

-- 索引
CREATE INDEX idx_media_media_type ON media(media_type);
CREATE INDEX idx_media_category_id ON media(category_id);
CREATE INDEX idx_media_created_at ON media(created_at);

-- 初始化数据
INSERT INTO category (name, description) VALUES
('摄影', '摄影作品'),
('视频', '视频作品'),
('生活', '生活记录'),
('旅行', '旅行见闻'),
('艺术', '艺术创作');

INSERT INTO tag (name) VALUES
('风景'),
('人像'),
('动物'),
('建筑'),
('美食'),
('运动'),
('音乐'),
('电影');
