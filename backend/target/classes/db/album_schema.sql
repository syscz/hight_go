CREATE TABLE IF NOT EXISTS albums (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    cover_image VARCHAR(500),
    media_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE media ADD COLUMN album_id BIGINT;

ALTER TABLE media ADD CONSTRAINT fk_media_album
    FOREIGN KEY (album_id) REFERENCES albums(id)
    ON DELETE SET NULL;

INSERT INTO albums (name, description, media_count) VALUES ('默认相册', '系统默认相册', 0);
