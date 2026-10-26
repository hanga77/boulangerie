# Phase 6 — Unifier trésorerie & reporting

**Statut :** en cours  
**Branche :** `refonte/phase-1`

## Objectif

Un seul vocabulaire de transactions et des totaux CA qui incluent **V1 et V2**.

## Backlog

### P0 — Types & calculs (ce commit)
- [x] `TransactionTypes` — catalogue des types revenus / dépenses
- [x] `FinanceService` — revenus = somme de tous les types CA (dont `VENTE_LIBRE`, V2)
- [x] Types normalisés V2 à l’écriture : `VENTE_LIVRAISON_V2`, `VENTE_CAISSE_V2`

### P1 — Reporting unifié
- [ ] Service `UnifiedReportingService` (CA jour / période par source)
- [ ] Écran `/cycle/tresorerie` ou `/comptabilite/ca-unifie`

### P2 — Dashboard
- [ ] KPI dashboard branchés sur les mêmes formules unifiées

## Hors scope

- Plan comptable / partie double OHADA
