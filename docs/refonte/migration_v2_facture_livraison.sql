-- Phase 3 P2 — lien facture sur livraison_v2

ALTER TABLE livraison_v2
    ADD COLUMN IF NOT EXISTS facture_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS facture_numero VARCHAR(64) NULL;

-- MySQL < 8 n'a pas IF NOT EXISTS sur ADD COLUMN : en cas d'erreur "duplicate column", ignorer.
