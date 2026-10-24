# Phase 2 — Clôture

**Statut :** clôturée (périmètre cycle métier V2)
**Branche :** `refonte/phase-1`
**Date de clôture :** 2026-10-24

## Périmètre livré et accepté

Cycle journalier complet sur **ProductionV2** :

1. Lancement production (commandes + vente libre + **farinage**)
2. Feuille de sortie magasinier (théorique connu dès le lancement)
3. Confirmation des sorties stock
4. Validation quantités produites
5. **Livraisons** liées à ProductionV2 (`livraison_v2`)
6. **Ventes libres** liées à ProductionV2 (`vente_libre_v2`)
7. Tableau de bord `/cycle` + clôture de journée
8. API REST `/api/cycle/*` (phase 1) + écrans Thymeleaf (phase 2)

## Migrations requises

```bash
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2.sql
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2_sales.sql
```

## Entrées utilisateur

| URL | Usage |
|-----|--------|
| `/cycle` | Pilotage du jour |
| `/cycle/lancer` | Boulanger |
| `/cycle/feuille/{id}` | Magasinier |
| `/cycle/valider/{id}` | Boulanger |
| `/cycle/livraison/{id}` | Manager |
| `/cycle/vente/{id}` | Manager / Caissier |

## Explicitement hors phase 2 (reporté)

- Bascule / décommissionnement de l’ancien `/production`, `/livraisons`, `/ventes-libres`
- Facture PDF automatique sur livraison V2
- Guichet POS complet branché sur ProductionV2
- CMP (coût moyen pondéré)
- Activation par défaut de `SecurityConfigV2`
- Suite de tests d’intégration automatisés

→ Ces points constituent le démarrage éventuel d’une **phase 3**.

## Critère de clôture

Un atelier peut enchaîner une journée réelle sur le cycle V2 sans passer par les services Production V1 pour :
lancer → sortir matières (dont farinage) → valider → livrer / vendre → clôturer.

**Phase 2 = clôturée sur ce critère.**
