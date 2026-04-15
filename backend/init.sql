INSERT INTO users (id, username, password, role, created_at, updated_at) VALUES (1, 'admin', '123456', 'ADMIN', NOW(), NOW()) ON DUPLICATE KEY UPDATE username = username;
UPDATE albums SET user_id = 1 WHERE user_id IS NULL OR user_id = 0;
UPDATE media SET user_id = 1 WHERE user_id IS NULL OR user_id = 0;