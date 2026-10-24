# Phase 4 — Mise en production & exploitation

**Statut :** en cours  
**Branche :** `refonte/phase-1`  
**Démarrage :** 2026-10-24

## Objectif

Rendre le cycle V2 **prêt pour un déploiement réel** : tableaux de bord orientés cycle,
traçabilité stock → ProductionV2, docs d’exploitation, tests élargis.

## Backlog

### P0 — Démarrage (ce commit)
- [x] Roadmap phase 4
- [x] Dashboard : actions rapides centrées sur le **Cycle du jour**
- [x] `stock_movement.production_v2_id` (traçabilité sorties V2)
- [x] Adapter inventaire : enregistre le lien ProductionV2 sur les sorties

### P1 — Traçabilité & données
- [ ] Rapport simple « sorties du jour » filtrable par production V2
- [ ] Écran historique livraisons V2 (liste globale)

### P2 — Exploitation
- [ ] Checklist déploiement (`docs/refonte/DEPLOIEMENT.md`)
- [ ] Profil `prod` documenté (variables d’env)

### P3 — Qualité élargie
- [ ] Tests `@DataJpaTest` repository ProductionV2 (H2)
- [ ] Élargir CI si besoin

## Hors phase 4

- Suppression totale des modules V1
- Multi-tenant / multi-sites
- App mobile
