-- =============================================================================
-- Gestiboul — Migration V2 COMPLÈTE (tout-en-un)
-- Branche : refonte/phase-1
-- Prérequis : backup de boulangerie_bd
-- Usage :
--   mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_all.sql
-- Idempotent : ré-exécutable sans casser si tables/colonnes existent déjà.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Production V2 + lignes + lots
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS production_v2 (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_production DATE         NOT NULL,
    statut          VARCHAR(20)  NOT NULL DEFAULT 'PLANIFIEE',
    user_id         BIGINT       NOT NULL,
    CONSTRAINT fk_prod_v2_user FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS production_ligne_produit (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    production_id       BIGINT  NOT NULL,
    produit_id          BIGINT  NOT NULL,
    quantite_planifiee  INT     NOT NULL,
    quantite_reelle     INT     NOT NULL,
    quantite_restante   INT     NOT NULL,
    CONSTRAINT fk_plp_prod FOREIGN KEY (production_id) REFERENCES production_v2(id) ON DELETE CASCADE,
    CONSTRAINT fk_plp_produit FOREIGN KEY (produit_id) REFERENCES produit(id)
);

CREATE TABLE IF NOT EXISTS production_ligne_matiere (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    production_id         BIGINT       NOT NULL,
    matiere_premiere_id   BIGINT       NOT NULL,
    quantite_theorique    DOUBLE       NOT NULL,
    quantite_magasinier   DOUBLE       NULL,
    quantite_boulanger    DOUBLE       NULL,
    CONSTRAINT fk_plm_prod FOREIGN KEY (production_id) REFERENCES production_v2(id) ON DELETE CASCADE,
    CONSTRAINT fk_plm_mat  FOREIGN KEY (matiere_premiere_id) REFERENCES matiere_premiere(id)
);

CREATE TABLE IF NOT EXISTS lot_v2 (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    matiere_premiere_id   BIGINT       NOT NULL,
    quantite              DOUBLE       NOT NULL,
    quantite_restante     DOUBLE       NOT NULL,
    prix_unitaire_entree  DOUBLE       NOT NULL,
    date_peremption       DATE         NULL,
    date_entree           DATE         NOT NULL,
    fournisseur_id        BIGINT       NULL,
    CONSTRAINT fk_lot_mat FOREIGN KEY (matiere_premiere_id) REFERENCES matiere_premiere(id)
);

-- Indexes production (ignorer si existent)
CREATE INDEX idx_prod_v2_date   ON production_v2 (date_production);
CREATE INDEX idx_prod_v2_statut ON production_v2 (statut);
CREATE INDEX idx_plp_production ON production_ligne_produit (production_id);
CREATE INDEX idx_plp_produit    ON production_ligne_produit (produit_id);
CREATE INDEX idx_plm_production ON production_ligne_matiere (production_id);
CREATE INDEX idx_plm_matiere    ON production_ligne_matiere (matiere_premiere_id);
CREATE INDEX idx_lot_matiere    ON lot_v2 (matiere_premiere_id);
CREATE INDEX idx_lot_dlc        ON lot_v2 (date_peremption);

-- -----------------------------------------------------------------------------
-- 2. Livraisons V2 (+ facture) et ventes V2
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS livraison_v2 (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_livraison      DATE         NOT NULL,
    nom_client          VARCHAR(255) NOT NULL,
    point_de_vente_id   BIGINT       NULL,
    production_v2_id    BIGINT       NOT NULL,
    user_id             BIGINT       NOT NULL,
    montant_total       DOUBLE       NOT NULL DEFAULT 0,
    revenu_enregistre   BOOLEAN      NOT NULL DEFAULT FALSE,
    facture_id          BIGINT       NULL,
    facture_numero      VARCHAR(64)  NULL,
    CONSTRAINT fk_liv_v2_prod FOREIGN KEY (production_v2_id) REFERENCES production_v2(id)
);

CREATE TABLE IF NOT EXISTS livraison_ligne_v2 (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    livraison_id    BIGINT NOT NULL,
    produit_id      BIGINT NOT NULL,
    quantite        INT    NOT NULL,
    prix_vente      DOUBLE NOT NULL,
    CONSTRAINT fk_ll_v2_liv FOREIGN KEY (livraison_id) REFERENCES livraison_v2(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS vente_libre_v2 (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_vente          DATE         NOT NULL,
    production_v2_id    BIGINT       NOT NULL,
    user_id             BIGINT       NOT NULL,
    guichet_id          BIGINT       NULL,
    moyen_paiement      VARCHAR(20)  NULL,
    montant_total       DOUBLE       NOT NULL DEFAULT 0,
    CONSTRAINT fk_vente_v2_prod FOREIGN KEY (production_v2_id) REFERENCES production_v2(id)
);

CREATE TABLE IF NOT EXISTS vente_ligne_v2 (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    vente_id        BIGINT NOT NULL,
    produit_id      BIGINT NOT NULL,
    quantite        INT    NOT NULL,
    prix_unitaire   DOUBLE NOT NULL,
    CONSTRAINT fk_vl_v2_vente FOREIGN KEY (vente_id) REFERENCES vente_libre_v2(id) ON DELETE CASCADE
);

CREATE INDEX idx_liv_v2_prod ON livraison_v2 (production_v2_id);
CREATE INDEX idx_liv_v2_date ON livraison_v2 (date_livraison);
CREATE INDEX idx_vente_v2_prod ON vente_libre_v2 (production_v2_id);
CREATE INDEX idx_vente_v2_date ON vente_libre_v2 (date_vente);

-- -----------------------------------------------------------------------------
-- 3. Colonnes manquantes si tables déjà créées sans facture / stock V2
--    (compatible MySQL 5.7 / 8 — pas de ADD COLUMN IF NOT EXISTS)
-- -----------------------------------------------------------------------------

-- livraison_v2.facture_id
SET @db := DATABASE();
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'livraison_v2' AND COLUMN_NAME = 'facture_id'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE livraison_v2 ADD COLUMN facture_id BIGINT NULL',
  'SELECT ''facture_id already exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- livraison_v2.facture_numero
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'livraison_v2' AND COLUMN_NAME = 'facture_numero'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE livraison_v2 ADD COLUMN facture_numero VARCHAR(64) NULL',
  'SELECT ''facture_numero already exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- stock_movement.production_v2_id
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'stock_movement' AND COLUMN_NAME = 'production_v2_id'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE stock_movement ADD COLUMN production_v2_id BIGINT NULL',
  'SELECT ''production_v2_id already exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Index stock_movement.production_v2_id
SET @exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'stock_movement' AND INDEX_NAME = 'idx_stock_movement_prod_v2'
);
SET @sql := IF(@exists = 0,
  'CREATE INDEX idx_stock_movement_prod_v2 ON stock_movement (production_v2_id)',
  'SELECT ''idx_stock_movement_prod_v2 already exists'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================================================
-- Fin migration V2
-- Vérification rapide :
--   SHOW TABLES LIKE '%_v2';
--   SHOW COLUMNS FROM stock_movement LIKE 'production_v2_id';
--   SHOW COLUMNS FROM livraison_v2 LIKE 'facture%';
-- =============================================================================
