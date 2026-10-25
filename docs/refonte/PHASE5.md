# Phase 5 — Consolidation métier & préparation release

**Statut :** en cours  
**Branche :** `refonte/phase-1`

## Backlog

### P0 — Démarrage ✅
- Historique ventes V2 — `/cycle/ventes-v2`

### P1 — Suivi & reporting ✅
- Synthèse journalière — `/cycle/synthese`

### P2 — Tests flux ✅
- [x] `CycleFluxDomaineTest` : valider → magasin → livraison/vente → débit restants → clôture

```bash
./mvnw -B test -Dtest=CycleFluxDomaineTest,ProductionV2Test,LivraisonV2Test,VenteLibreV2Test
```

### P3 — Release
- [ ] Notes de version `docs/refonte/RELEASE_NOTES.md`
- [ ] Checklist merge `refonte/phase-1` → `master`
