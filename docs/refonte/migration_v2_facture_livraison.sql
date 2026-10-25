-- Phase 3 P2 — lien facture sur livraison_v2 (idempotent MySQL 5.7+)

SET @db := DATABASE();

SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'livraison_v2' AND COLUMN_NAME = 'facture_id'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE livraison_v2 ADD COLUMN facture_id BIGINT NULL',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'livraison_v2' AND COLUMN_NAME = 'facture_numero'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE livraison_v2 ADD COLUMN facture_numero VARCHAR(64) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
