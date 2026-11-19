-- Migration Gestiboul V2 — Production + Lots
-- À exécuter après backup de boulangerie_bd

CREATE TABLE IF NOT EXISTS production_v2 (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_production DATE         NOT NULL,
    statut          VARCHAR(20)  NOT NULL DEFAULT 'PLANIFIEE',
    user_id         BIGINT       NOT NULL,
    CONSTRAINT fk_prod_v2_user FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE INDEX idx_prod_v2_date   ON production_v2 (date_production);
CREATE INDEX idx_prod_v2_statut ON production_v2 (statut);

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

CREATE INDEX idx_plp_production ON production_ligne_produit (production_id);
CREATE INDEX idx_plp_produit    ON production_ligne_produit (produit_id);

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

CREATE INDEX idx_plm_production ON production_ligne_matiere (production_id);
CREATE INDEX idx_plm_matiere    ON production_ligne_matiere (matiere_premiere_id);

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

CREATE INDEX idx_lot_matiere ON lot_v2 (matiere_premiere_id);
CREATE INDEX idx_lot_dlc     ON lot_v2 (date_peremption);
