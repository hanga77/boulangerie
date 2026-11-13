-- =============================================================================
-- Pack migrations complet Gestiboul (V2 + pilote phase 7)
-- Prérequis : base existante (tables métier V1 déjà présentes)
-- =============================================================================

SOURCE docs/refonte/migration_v2_all.sql;

-- Note: MySQL CLI n'exécute pas SOURCE relatif de la même façon partout.
-- Préférez enchaîner les deux commandes shell documentées dans GENERATION.md.
-- Ci-dessous : table pilote (idempotent) si vous exécutez ce fichier seul après V2.

CREATE TABLE IF NOT EXISTS pilot_observation (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_jour    DATE         NOT NULL,
    etape        VARCHAR(40)  NOT NULL,
    ok           BOOLEAN      NOT NULL DEFAULT TRUE,
    commentaire  VARCHAR(2000) NULL,
    auteur       VARCHAR(80)  NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pilot_obs_date (date_jour),
    INDEX idx_pilot_obs_etape (etape)
);
