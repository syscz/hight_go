-- 用户数据迁移脚本
-- 创建admin用户 (ID=1)
INSERT INTO users (id, username, password, role, created_at, updated_at)
VALUES (1, 'admin', '123456', 'ADMIN', NOW(), NOW())
ON DUPLICATE KEY UPDATE username = username;

-- 为albums表添加user_id列（如果不存在）
ALTER TABLE albums ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- 为media表添加user_id列（如果不存在）
ALTER TABLE media ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- 将所有现有相册归属到admin用户
UPDATE albums SET user_id = 1 WHERE user_id IS NULL OR user_id = 0;

-- 将所有现有媒体归属到admin用户
UPDATE media SET user_id = 1 WHERE user_id IS NULL OR user_id = 0;