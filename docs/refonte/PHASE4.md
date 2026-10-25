# Phase 4 — Mise en production & exploitation

**Statut :** en cours  
**Branche :** `refonte/phase-1`

## Backlog

### P0 — Démarrage ✅
- Dashboard centré cycle
- `stock_movement.production_v2_id`

### P1 — Traçabilité & données ✅
- `/cycle/sorties` — sorties stock filtrables
- `/cycle/livraisons-v2` — historique livraisons V2

### P2 — Exploitation ✅
- [x] Checklist déploiement : [DEPLOIEMENT.md](./DEPLOIEMENT.md)
- [x] Profil Spring `application-prod.properties` (secrets via env)
- [x] Exemple env : [application-prod.properties.example](./application-prod.properties.example)

Activation :
```bash
export SPRING_PROFILES_ACTIVE=prod
export LICENSE_SALT="$(openssl rand -hex 32)"
export DB_USERNAME=...
export DB_PASSWORD=...
./mvnw spring-boot:run
```

### P3 — Qualité élargie
- [ ] Tests `@DataJpaTest` ProductionV2 (H2)
