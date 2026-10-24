# Cycle journalier Gestiboul

Flux opérationnel recommandé pour une journée de production.

```
┌─────────────┐    ┌──────────────┐    ┌──────────────┐    ┌─────────────┐
│  1.COMMANDES │───►│ 2.PRODUCTION │───►│ 3.VALIDATION │───►│ 4.SORTIE    │
│   Manager    │    │  Boulanger   │    │  Boulanger   │    │  Magasinier │
└─────────────┘    └──────────────┘    └──────────────┘    └─────────────┘
                                                                  │
                   ┌──────────────┐    ┌──────────────┐           │
                   │ 7.CLÔTURE    │◄───│ 6.VENTES     │◄──────────┘
                   │   Manager    │    │  Caissier    │    ┌─────────────┐
                   └──────────────┘    └──────────────┘◄───│ 5.LIVRAISONS│
                                                           │   Manager   │
                                                           └─────────────┘
```

## Étapes détaillées

### 1. Commandes (MANAGER)
- Saisie des commandes par point de vente pour la date du jour (ou J+1).
- Chaque ligne = produit + quantité.
- **Use case / écran** : module commandes existant.

### 2. Lancement production (BOULANGER)
- Agrège toutes les commandes du jour + quotas vente libre.
- Calcule le **théorique matières** à partir des recettes.
- Crée `ProductionV2` en statut `EN_COURS`.
- **Use case** : `StartDailyProductionUseCase`

### 3. Validation quantités (BOULANGER)
- Saisie des quantités réellement sorties du four.
- Optionnel : quantités matières déclarées côté fournil.
- Comparaison théorique / réel → détection d'écarts (seuil configurable, défaut 10 %).
- Statut → `VALIDEE`.
- **Use case** : `ValidateProductionQuantitiesUseCase`

### 4. Sortie stock (MAGASINIER)
- Pour chaque matière de la production, confirmer la quantité réellement sortie du magasin.
- Obligatoirement liée à la `productionId`.
- Le théorique est affiché en repère.
- **Use case** : `ConfirmStockExitForProductionUseCase`
- La checklist « sorties complètes » = toutes les lignes matière ont un `quantiteMagasinier`.

### 5. Livraisons (MANAGER)
- Création des livraisons vers les points de vente à partir des quantités restantes.
- Facture PDF automatique (module existant).
- Décrémente `quantiteRestante` des lignes produit.

### 6. Ventes guichet (CAISSIER)
- Vente libre sur le stock restant de la production du jour (ou veille).
- Espèces / Mobile Money.
- Ticket thermique.

### 7. Clôture (MANAGER)
- Vérifie production validée + sorties stock complètes (sauf `forcer=true`).
- Passe la production en `CLOTUREE`.
- **Use case** : `CloseDailyCycleUseCase`

## Tableau de bord

`GetDailyCycleStatusUseCase` expose un `DailyCycleSnapshot` :

| Champ | Signification |
|-------|----------------|
| `etapeCourante` | Prochaine étape à faire |
| `checklist` | Map étape → fait / pas fait |
| `nbLignesMatiereConfirmees / nbLignesMatiere` | Avancement magasinier |
| `quantiteProduitsRestants` | Encore vendable / livrable |

À brancher sur le dashboard (`/dashboard`) pour un pilotage en temps réel.

## Règles métier importantes

1. **Une seule production par jour** (contrainte dans `StartDailyProductionUseCase`).
2. **Sortie stock sans production** = interdite pour les SORTIE liées (traçabilité).
3. **Clôture bloquée** si validation ou sorties incomplètes, sauf forçage manager.
4. Les ventes guichet peuvent démarrer dès que la production est validée (pas besoin d'attendre toutes les livraisons).

## Enchaînement API / services (cible)

```
StartDailyProductionUseCase
    → ValidateProductionQuantitiesUseCase
        → ConfirmStockExitForProductionUseCase (× N matières)
            → (livraisons / ventes — modules existants)
                → CloseDailyCycleUseCase
```

Suivi permanent : `GetDailyCycleStatusUseCase.execute(LocalDate.now())`.
