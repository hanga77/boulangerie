# Guide d'intégration de la refonte

## Prérequis

- Backup MySQL complet
- Branche : `refonte/phase-1`

## Étape 1 — Migration SQL

```bash
mysql -u root -p boulangerie_bd < docs/refonte/migration_v2.sql
```

## Étape 2 — Brancher les ports (adapters JPA)

Créer les implémentations :

- `InventoryPort` → `JpaInventoryAdapter`
- `FinancePort` → `JpaFinanceAdapter`
- `ProductionPort` / `ProductionPersistencePort` → repositories `ProductionV2`

**Important** : tant que les adapters ne sont pas fournis, les `@Service` use cases ne démarreront pas (beans manquants). Soit implémenter les ports, soit exclure temporairement le scan des packages `application` / `domain` / `infrastructure`.

## Étape 3 — Remplacer progressivement

| Ancien | Nouveau |
|--------|--------|
| `StockService.addStock(...)` | `ReceiveStockUseCase.execute(...)` |
| `StockService.removeStockForProduction(...)` | `ConfirmStockExitForProductionUseCase.execute(...)` |
| `ProductionService.startProduction(...)` | `StartDailyProductionUseCase.execute(...)` |
| `SecurityConfig` | `SecurityConfigV2` (renommer l'ancien en `.legacy`) |
| `LicenseService` | `LicenseServiceV2` |

## Étape 4 — Configuration prod

```bash
export LICENSE_SALT="$(openssl rand -hex 32)"
```

Voir `application-prod.properties.example`.

## Attention collision beans

`SecurityConfigV2` et l'ancien `SecurityConfig` ne peuvent pas coexister (deux `SecurityFilterChain`). Désactiver l'ancien avant d'activer le nouveau.

Même chose pour `LoginRateLimitFilter` (package `security` vs `infrastructure.security`).
