-- Phase 7 — observations pilote terrain
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
