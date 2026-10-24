# Phase 3 — Industrialisation du cycle V2

**Statut :** en cours  
**Branche :** `refonte/phase-1`  
**Démarrage :** 2026-10-24

## Objectif

Faire du cycle V2 le **flux principal** en conditions réelles : caisse, sécu, documents, bascule progressive V1.

## Backlog priorisé

### P0 — Démarrage (ce commit)
- [x] Roadmap phase 3
- [x] **Caisse / POS V2** sur stock restant ProductionV2 (`/cycle/caisse`)
- [x] Property `app.refonte.security-v2` documentée (activation progressive)

### P1 — Sécurité prod
- [ ] Fusion / activation `SecurityConfigV2` sans casser le login
- [ ] Licence V2 + `LICENSE_SALT` obligatoire en profil `prod`
- [ ] Rate-limit login actif par défaut

### P2 — Documents
- [ ] PDF ticket / facture liée à `livraison_v2`
- [ ] Affichage numéro facture sur écran livraison V2

### P3 — Bascule UX
- [ ] Menu : Cycle du jour en premier pour rôles atelier
- [ ] Bandeau « legacy » sur `/production` et `/livraisons` pointant vers `/cycle`
- [ ] FK stock `production_v2_id` (migration colonne optionnelle)

### P4 — Qualité
- [ ] Tests d’intégration cycle
- [ ] CI GitHub Actions (build + test)

## Entrées phase 3 livrées

| URL | Rôle |
|-----|------|
| `/cycle/caisse` | Caissier / Manager — POS sur production du jour ou veille |
| `/cycle/caisse/{productionId}` | POS ciblé sur une production V2 |
