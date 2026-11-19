# Phase 2 — Clôture définitive

**Statut :** TERMINÉE  
**Branche :** `refonte/phase-1`  
**Date :** 2026-10-24

## Livrables phase 2

### Cycle métier V2 (UI + domaine)
- [x] Tableau de bord `/cycle`
- [x] Lancement production + farinage dans le théorique
- [x] Feuille de sortie magasinier
- [x] Confirmation sorties stock
- [x] Validation quantités produites
- [x] Clôture de journée

### Livraisons & ventes V2
- [x] `livraison_v2` / `livraison_ligne_v2` liées à `production_v2`
- [x] `vente_libre_v2` / `vente_ligne_v2` liées à `production_v2`
- [x] Débit de `quantiteRestante` sur les lignes produit
- [x] Écrans `/cycle/livraison/{id}` et `/cycle/vente/{id}`
- [x] **Facture automatique** à chaque livraison V2 (`FacturationService`)
- [x] Enregistrement revenu optionnel (crédit compte + transaction)

### Stock
- [x] **CMP (coût moyen pondéré)** à l’entrée de stock (`JpaInventoryAdapter.augmenterStock`)
- [x] Lots V2 + DLC

### Infra phase 1 (rappel)
- [x] Adapters JPA, API `/api/cycle/*`, événements stock↔finance

## Migrations

```bash
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2.sql
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_sales.sql
```

## Hors phase 2 → phase 3

| Sujet | Pourquoi reporté |
|-------|------------------|
| Décommissionnement total V1 (`/production`, `/livraisons`…) | Coexistence volontaire ; bascule progressive |
| Guichet POS tactile complet sur V2 | Module séparé (phase 3) |
| PDF ticket thermique livraison V2 | Facture enregistrée ; rendu PDF UI existant `/finances` |
| `SecurityConfigV2` par défaut | Profil `refonte-security` prêt |
| Suite de tests d’intégration CI | À brancher en phase 3 |

## Critère d’acceptation phase 2 — OK

Enchaînement réel possible uniquement via le cycle V2 :

```
Commandes → Lancer (+farinage) → Sortie magasin → Valider
         → Livrer (facture auto) / Vendre → Clôturer
```

**La phase 2 est terminée.**
