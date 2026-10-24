# Phase 3 — Industrialisation du cycle V2

**Statut :** TERMINÉE (P0–P4)  
**Branche :** `refonte/phase-1`

## Backlog

### P0 — Démarrage ✅
- Caisse POS V2 (`/cycle/caisse`)

### P1 — Sécurité prod ✅
- SecurityConfig durci, rate-limit 429, ProdLicenseGuard

### P2 — Documents ✅
- Facture liée livraison V2 + PDF

### P3 — Bascule UX ✅ (progressive)
- Menu Cycle / Caisse en avant ; Historique pour V1
- Bandeaux legacy sur production & livraisons principales
- **Pas toutes les interfaces** d’un coup : les autres écrans V1 restent utilisables ;
  on enrichira les bandeaux au fil des besoins.

### P4 — Qualité ✅
- [x] Tests unitaires domaine : `ProductionV2`, `LivraisonV2`, `DailyCycleStep`
- [x] CI GitHub Actions : `.github/workflows/ci.yml` (JDK 17, `mvn test` + package)
  sur `master` / `main` / `refonte/**` et les PR

```bash
./mvnw test
```

## Note UX progressive

La bascule V1→V2 est **progressive** :
- flux quotidien recommandé = `/cycle`
- modules V1 conservés pour historique / compatibilité
- bandeaux sur les écrans les plus utilisés d’abord
