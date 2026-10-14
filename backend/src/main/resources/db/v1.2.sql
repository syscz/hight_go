-- v1.2: 视频元数据 —— 时长（秒），配合转码任务的封面帧提取
-- MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，重复执行会报列已存在，属预期
ALTER TABLE media ADD COLUMN duration INT DEFAULT NULL;
