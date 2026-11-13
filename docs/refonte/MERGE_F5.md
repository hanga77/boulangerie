# F5 — Merger `refonte/phase-1` dans `master`

## Contenu de la branche

- Cycle journalier V2 (prod → magasin → livraisons → caisse → clôture)
- Finance unifiée, reporting CSV, multi-comptes, marge
- UI unifiée progressive + bandeaux V1
- Pilote terrain, health, readiness Compte Principal

## Avant merge

1. CI verte  
2. Pilote terrain (F4)  
3. Backup prod  
4. Plan de migration SQL  

## Après merge

```bash
mysql ... < docs/refonte/migration_v2_all.sql
mysql ... < docs/refonte/migration_pilot.sql
# Créer Compte Principal si besoin (UI ADMIN)
java -jar ...
```

Voir aussi `GENERATION.md` et `DEPLOIEMENT.md`.
