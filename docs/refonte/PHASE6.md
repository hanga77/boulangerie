# Phase 6 — Trésorerie & reporting unifiés

**Branche :** `refonte/phase-1`

## Corrections « norme opérationnelle »

| Sujet | Avant | Après |
|-------|-------|-------|
| Charges fixes | Type `CHARGE` hors totaux dépenses | Inclus dans `EXPENSE_TYPES` |
| Dettes fournisseurs | Remboursement sans journal | Type `DETTE_REMBOURSEMENT` + débit compte |
| Marge production | Impossible | `/cycle/marge` — CA − coût matières |

## Formule marge production V2

```
marge = (CA livraisons V2 + CA caisse V2) − Σ (qté matière × prix unitaire stock)
```

- Qté matière : boulanger > magasinier > théorique
- Charges fixes **non ventilées** par lot (structure)
- Dettes en cours = engagement, pas une dépense tant que non payées

## URLs

| URL | Rôle |
|-----|------|
| `/cycle/tresorerie` | CA consolidé |
| `/cycle/depenses` | Saisie dépenses |
| `/cycle/recettes` | Saisie recettes manuelles |
| `/cycle/marge` | Marge / gain-perte par production du jour |
| `/cycle/marge/{id}` | Détail d'une production |
