-- Livraisons & ventes liées à ProductionV2

CREATE TABLE IF NOT EXISTS livraison_v2 (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_livraison      DATE         NOT NULL,
    nom_client          VARCHAR(255) NOT NULL,
    point_de_vente_id   BIGINT       NULL,
    production_v2_id    BIGINT       NOT NULL,
    user_id             BIGINT       NOT NULL,
    montant_total       DOUBLE       NOT NULL DEFAULT 0,
    revenu_enregistre   BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_liv_v2_prod FOREIGN KEY (production_v2_id) REFERENCES production_v2(id)
);

CREATE INDEX idx_liv_v2_prod ON livraison_v2 (production_v2_id);
CREATE INDEX idx_liv_v2_date ON livraison_v2 (date_livraison);

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

CREATE INDEX idx_vente_v2_prod ON vente_libre_v2 (production_v2_id);
CREATE INDEX idx_vente_v2_date ON vente_libre_v2 (date_vente);

CREATE TABLE IF NOT EXISTS vente_ligne_v2 (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    vente_id        BIGINT NOT NULL,
    produit_id      BIGINT NOT NULL,
    quantite        INT    NOT NULL,
    prix_unitaire   DOUBLE NOT NULL,
    CONSTRAINT fk_vl_v2_vente FOREIGN KEY (vente_id) REFERENCES vente_libre_v2(id) ON DELETE CASCADE
);
