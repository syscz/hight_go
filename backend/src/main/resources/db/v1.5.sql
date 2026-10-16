-- v1.5 封面尺寸（广场瀑布流 aspect-ratio 占位防加载跳动）
-- 执行方式：mysql -u root -p media_website < v1.5.sql

ALTER TABLE media ADD COLUMN cover_width INT DEFAULT NULL;
ALTER TABLE media ADD COLUMN cover_height INT DEFAULT NULL;

-- 存量数据回填：用 scripts/backfill-cover-size.py（服务器/本机均可运行）
