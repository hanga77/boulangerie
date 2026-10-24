# Phase 3 — Industrialisation du cycle V2

**Statut :** en cours  
**Branche :** `refonte/phase-1`

## Objectif

Faire du cycle V2 le flux principal en conditions réelles.

## Backlog

### P0 — Démarrage
- [x] Roadmap phase 3
- [x] Caisse POS V2 (`/cycle/caisse`)
- [x] Property sécu documentée

### P1 — Sécurité prod ✅
- [x] `SecurityConfig` durci (headers CSP, sessions, BCrypt 12)
- [x] Routes `/cycle/**` et `/api/cycle/**` autorisées par rôle
- [x] Hiérarchie MAGASINIER + CAISSIER sous MANAGER
- [x] Swagger fermé en profil `prod`
- [x] Rate-limit login → HTTP **429** (actif par défaut)
- [x] `ProdLicenseGuard` : salt obligatoire et fort en profil `prod`

Activation prod :
```bash
export LICENSE_SALT="$(openssl rand -hex 32)"
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

### P2 — Documents
- [ ] PDF / n° facture lié à `livraison_v2`

### P3 — Bascule UX
- [ ] Menus centrés cycle ; bandeaux legacy

### P4 — Qualité
- [ ] Tests d’intégration + CI

## Note SecurityConfigV2

La classe `infrastructure.security.SecurityConfigV2` reste derrière le profil
`refonte-security` pour éviter une double `SecurityFilterChain`.
Le durcissement P1 a été **intégré dans** `security.SecurityConfig` (config active).
