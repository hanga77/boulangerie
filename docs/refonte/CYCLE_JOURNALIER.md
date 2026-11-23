# Cycle journalier Gestiboul

## Principe clé : la feuille de sortie magasinier

**Dès le lancement de la production**, le système connaît déjà tout ce que le
magasinier doit fournir au boulanger :

1. Matières calculées depuis les **recettes** des produits commandés
2. Matières des **quotas vente libre**
3. **Farinage du lot** (fleurage / farine de travail) saisi au lancement

→ Cette liste = `GetMaterialsToSupplyUseCase`  
→ La sortie magasin confirme ces quantités (avec ajustement possible) via
  `ConfirmAllTheoreticalExitsUseCase` ou ligne par ligne.

```
Commandes + recettes + farinage
        │
        ▼
 StartDailyProduction  ──────►  théorique matières FIGÉ
        │                              │
        │                              ▼
        │                    Feuille de sortie MAGASINIER
        │                    (y compris farinage du lot)
        │                              │
        ▼                              ▼
 Boulanger produit              Confirm sortie stock
        │                              │
        ▼                              │
 Validation quantités ◄────────────────┘
        │
        ▼
 Livraisons → Ventes guichet → Clôture
```

## Étapes

### 1. Commandes (MANAGER)
Saisie par point de vente (produit × quantité).

### 2. Lancement production (BOULANGER)
`StartDailyProductionUseCase`

- Agrège commandes du jour + quotas vente libre
- Calcule le théorique matières (recettes)
- **Ajoute le farinage du lot** (`farinageMatiereId` + `farinageQuantite`)
- Crée `ProductionV2` en `EN_COURS`

À partir de là, le magasinier peut ouvrir la feuille de sortie.

### 3. Sortie stock (MAGASINIER) — *peut se faire en parallèle / juste après le lancement*

`GetMaterialsToSupplyUseCase` → liste :

| Matière | Théorique | Déjà sorti | Reste à sortir | Stock | Farinage ? |
|---------|-----------|------------|----------------|-------|------------|
| Farine blé | 45 kg | 0 | 45 kg | 120 kg | oui (lot) |
| Levure | 800 g | 0 | 800 g | 2 kg | non |
| … | | | | | |

Le magasinier confirme :

- **tout le théorique d’un coup** : `ConfirmAllTheoreticalExitsUseCase`
- **ou ligne par ligne** : `ConfirmStockExitForProductionUseCase`

Le farinage fait partie du théorique : **il doit être sorti comme les autres matières**.

### 4. Validation quantités produites (BOULANGER)
`ValidateProductionQuantitiesUseCase` — quantités réelles sorties du four, détection d’écarts.

### 5. Livraisons (MANAGER)
Vers les points de vente, à partir des quantités restantes.

### 6. Ventes guichet (CAISSIER)
Reste de production du jour / veille.

### 7. Clôture (MANAGER)
`CloseDailyCycleUseCase` — exige en principe validation + sorties stock complètes.

## Ordre réel recommandé en atelier

```
Matin tôt
  Manager  : commandes (souvent la veille)
  Boulanger: lance la production (+ saisit le farinage du lot)
  Magasinier: voit tout de suite la feuille de sortie (recettes + farinage)
             et fournit le fournil
  Boulanger: fabrique, puis valide les quantités produites
Journée
  Manager  : livraisons
  Caissier : ventes
Soir
  Manager  : clôture
```

Le magasinier **n’attend pas** la fin de la cuisson pour savoir quoi sortir :
le théorique (commandes + farinage) est connu dès l’étape 2.

## Use cases liés

| Use case | Acteur |
|----------|--------|
| `StartDailyProductionUseCase` | Boulanger |
| `GetMaterialsToSupplyUseCase` | Magasinier (lecture feuille) |
| `ConfirmAllTheoreticalExitsUseCase` | Magasinier (sortie globale) |
| `ConfirmStockExitForProductionUseCase` | Magasinier (ligne à ligne) |
| `ValidateProductionQuantitiesUseCase` | Boulanger |
| `GetDailyCycleStatusUseCase` | Tous (dashboard) |
| `CloseDailyCycleUseCase` | Manager |
