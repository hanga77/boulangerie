# Phase 6 — Unifier trésorerie & reporting

**Statut :** TERMINÉE (P0–P4)  
**Branche :** `refonte/phase-1`

## Backlog

### P0 — Types & calculs ✅
### P1 — Reporting unifié ✅ — `/cycle/tresorerie`
### P2 — KPI dashboard ✅
### P3 — Saisie dépenses ✅ — `/cycle/depenses`
### P4 — Saisie recettes ✅ — `/cycle/recettes`
- Types manuels : `VENTE`, `VENTE_LIBRE`, `PAIEMENT_FACTURE`
- Les types V2 auto (`VENTE_LIVRAISON_V2`, `VENTE_CAISSE_V2`) restent réservés au cycle

## URLs finance unifiée

| URL | Rôle |
|-----|------|
| `/cycle/tresorerie` | CA consolidé |
| `/cycle/depenses` | Saisie dépenses |
| `/cycle/recettes` | Saisie recettes manuelles |
| `/dashboard` | KPI |

## Consigne métier

Ne pas saisir une recette manuelle **en plus** d'une livraison/caisse V2 déjà enregistrée.
