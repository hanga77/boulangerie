# Phase 4 — Mise en production & exploitation

**Statut :** TERMINÉE (P0–P3)  
**Branche :** `refonte/phase-1`

## Backlog

### P0 — Démarrage ✅
- Dashboard centré cycle
- `stock_movement.production_v2_id`

### P1 — Traçabilité & données ✅
- `/cycle/sorties` — sorties stock filtrables
- `/cycle/livraisons-v2` — historique livraisons V2

### P2 — Exploitation ✅
- [DEPLOIEMENT.md](./DEPLOIEMENT.md)
- `application-prod.properties`

### P3 — Qualité élargie ✅
- [x] `@DataJpaTest` ProductionV2 (H2) :
  `src/test/java/.../ProductionV2RepositoryDataJpaTest.java`
  - cascade lignes produit / matière
  - `findByDateProductionWithLignes`
  - validation + débit restant
  - confirmation magasinier

```bash
./mvnw -B test
```

## Synthèse phases

| Phase | Thème | Statut |
|-------|--------|--------|
| 1 | Fondations cycle V2 | OK |
| 2 | Livraisons / ventes / facture / CMP | OK |
| 3 | Caisse, sécu, docs, UX, CI | OK |
| 4 | Prod, traçabilité, déploiement, DataJpaTest | OK |
