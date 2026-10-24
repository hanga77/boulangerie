# Phase 3 — Industrialisation du cycle V2

**Statut :** en cours  
**Branche :** `refonte/phase-1`

## Backlog

### P0 — Démarrage ✅
- [x] Roadmap + caisse POS V2

### P1 — Sécurité prod ✅
- [x] SecurityConfig durci, rate-limit 429, ProdLicenseGuard

### P2 — Documents ✅
- [x] Colonnes `facture_id` / `facture_numero` sur `livraison_v2`
- [x] `FacturationService.creerFactureAndReturn` → numéro renvoyé et stocké
- [x] PDF facture : `GET /cycle/livraison/{id}/pdf`
- [x] Affichage n° facture + bouton PDF sur l'écran livraison V2

Migration :
```bash
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_facture_livraison.sql
```

### P3 — Bascule UX
- [ ] Menus centrés cycle ; bandeaux legacy

### P4 — Qualité
- [ ] Tests d’intégration + CI
