-- Phase 4 — lien stock_movement → production_v2 (idempotent MySQL 5.7+)

SET @db := DATABASE();

SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'stock_movement' AND COLUMN_NAME = 'production_v2_id'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE stock_movement ADD COLUMN production_v2_id BIGINT NULL',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'stock_movement' AND INDEX_NAME = 'idx_stock_movement_prod_v2'
);
SET @sql := IF(@exists = 0,
  'CREATE INDEX idx_stock_movement_prod_v2 ON stock_movement (production_v2_id)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
