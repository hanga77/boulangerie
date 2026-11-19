-- =============================================================================
-- Gestiboul V2 — Clés étrangères manquantes (idempotent)
-- Prérequis : migration_v2_all.sql (ou scripts séparés) déjà appliqués
-- Usage :
--   mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_fk.sql
-- =============================================================================

SET @db := DATABASE();

-- Helper pattern: add FK only if constraint name does not exist

-- --- livraison_v2.user_id → user(id) ---
SET @exists := (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = @db AND TABLE_NAME = 'livraison_v2'
    AND CONSTRAINT_NAME = 'fk_liv_v2_user' AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE livraison_v2 ADD CONSTRAINT fk_liv_v2_user FOREIGN KEY (user_id) REFERENCES user(id)',
  'SELECT ''fk_liv_v2_user exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- --- livraison_v2.point_de_vente_id → point_de_vente(id) ---
SET @exists := (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = @db AND TABLE_NAME = 'livraison_v2'
    AND CONSTRAINT_NAME = 'fk_liv_v2_pdv' AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE livraison_v2 ADD CONSTRAINT fk_liv_v2_pdv FOREIGN KEY (point_de_vente_id) REFERENCES point_de_vente(id) ON DELETE SET NULL',
  'SELECT ''fk_liv_v2_pdv exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- --- livraison_v2.facture_id → facture(id) ---
SET @exists := (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = @db AND TABLE_NAME = 'livraison_v2'
    AND CONSTRAINT_NAME = 'fk_liv_v2_facture' AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE livraison_v2 ADD CONSTRAINT fk_liv_v2_facture FOREIGN KEY (facture_id) REFERENCES facture(id) ON DELETE SET NULL',
  'SELECT ''fk_liv_v2_facture exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- --- livraison_ligne_v2.produit_id → produit(id) ---
SET @exists := (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = @db AND TABLE_NAME = 'livraison_ligne_v2'
    AND CONSTRAINT_NAME = 'fk_ll_v2_produit' AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE livraison_ligne_v2 ADD CONSTRAINT fk_ll_v2_produit FOREIGN KEY (produit_id) REFERENCES produit(id)',
  'SELECT ''fk_ll_v2_produit exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- --- vente_libre_v2.user_id → user(id) ---
SET @exists := (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = @db AND TABLE_NAME = 'vente_libre_v2'
    AND CONSTRAINT_NAME = 'fk_vente_v2_user' AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE vente_libre_v2 ADD CONSTRAINT fk_vente_v2_user FOREIGN KEY (user_id) REFERENCES user(id)',
  'SELECT ''fk_vente_v2_user exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- --- vente_libre_v2.guichet_id → guichet(id) ---
SET @exists := (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = @db AND TABLE_NAME = 'vente_libre_v2'
    AND CONSTRAINT_NAME = 'fk_vente_v2_guichet' AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE vente_libre_v2 ADD CONSTRAINT fk_vente_v2_guichet FOREIGN KEY (guichet_id) REFERENCES guichet(id) ON DELETE SET NULL',
  'SELECT ''fk_vente_v2_guichet exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- --- vente_ligne_v2.produit_id → produit(id) ---
SET @exists := (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = @db AND TABLE_NAME = 'vente_ligne_v2'
    AND CONSTRAINT_NAME = 'fk_vl_v2_produit' AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE vente_ligne_v2 ADD CONSTRAINT fk_vl_v2_produit FOREIGN KEY (produit_id) REFERENCES produit(id)',
  'SELECT ''fk_vl_v2_produit exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- --- lot_v2.fournisseur_id → fournisseur(id) ---
SET @exists := (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = @db AND TABLE_NAME = 'lot_v2'
    AND CONSTRAINT_NAME = 'fk_lot_v2_fournisseur' AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE lot_v2 ADD CONSTRAINT fk_lot_v2_fournisseur FOREIGN KEY (fournisseur_id) REFERENCES fournisseur(id) ON DELETE SET NULL',
  'SELECT ''fk_lot_v2_fournisseur exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- --- stock_movement.production_v2_id → production_v2(id) ---
SET @exists := (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = @db AND TABLE_NAME = 'stock_movement'
    AND CONSTRAINT_NAME = 'fk_sm_prod_v2' AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE stock_movement ADD CONSTRAINT fk_sm_prod_v2 FOREIGN KEY (production_v2_id) REFERENCES production_v2(id) ON DELETE SET NULL',
  'SELECT ''fk_sm_prod_v2 exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================================================
-- Vérification :
-- SELECT TABLE_NAME, CONSTRAINT_NAME
-- FROM information_schema.TABLE_CONSTRAINTS
-- WHERE CONSTRAINT_SCHEMA = DATABASE() AND CONSTRAINT_TYPE = 'FOREIGN KEY'
--   AND TABLE_NAME IN ('livraison_v2','livraison_ligne_v2','vente_libre_v2',
--                      'vente_ligne_v2','lot_v2','stock_movement','production_v2');
-- =============================================================================
