-- Phase 4 — lien stock_movement → production_v2

ALTER TABLE stock_movement
    ADD COLUMN production_v2_id BIGINT NULL;

CREATE INDEX idx_stock_movement_prod_v2 ON stock_movement (production_v2_id);

-- Si la colonne existe déjà, ignorer l'erreur MySQL.
