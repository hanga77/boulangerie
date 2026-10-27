# Unification des interfaces

**Statut :** en cours (socle livré)  
**Branche :** `refonte/phase-1`

## Comparatif fait vs existant

| Domaine | Avant refonte | Après (état actuel) |
|---------|---------------|---------------------|
| Cycle journalier | Production V1 dispersée | `/cycle` V2 complet |
| Caisse / livraisons | Guichet + livraisons V1 | + Caisse/livraisons V2 liées à la prod |
| Trésorerie | Types `VENTE` partiels | Types unifiés + reporting + dépenses/recettes |
| Marge production | Absente | `/cycle/marge` |
| Dashboard | KPI V1 | KPI V1+V2 + liens cycle |
| UI globale | Bootstrap hétérogène | **Socle CSS commun** ; écrans V1 encore variables |
| Modules stock, PDV, RH, admin | Fonctionnels | **Logique intacte**, look à aligner par lots |

## Objectif UI

Rendre **toutes** les pages cohérentes via :
1. `styles.css` (design system)
2. `fragments/layout` (navbar, flash, scripts)
3. Conventions : `page-header`, `card` + `table`, empty states

## Lots

| Lot | Contenu | Statut |
|-----|---------|--------|
| U0 | Design system + menu Finance | ✅ |
| U1 | Login, register, erreurs | à faire |
| U2 | Commandes, produits, matières | à faire |
| U3 | Finances, compta, fournisseurs | à faire |
| U4 | Production/livraisons V1 (Historique) | à faire |
| U5 | Admin, employés, bulletins, guichet | à faire |

**Note :** tout réécrire d’un coup risque de casser des formulaires métier. On aligne par lots.
