# Phase 7 — Pilote terrain (build complet)

## Objectif

Valider le Cycle V2 en conditions réelles et capitaliser les écarts dans l’application.

## Livré

| Élément | URL / fichier |
|---------|----------------|
| Guide + checklist interactive | `/admin/pilot` |
| Saisie observation | `POST /admin/pilot/observation` |
| Liste observations | `/admin/pilot/observations` |
| Menu Admin | Pilote terrain · Observations pilote |
| Entité | `PilotObservation` |
| Migration | `docs/refonte/migration_pilot.sql` |

## Migration

```bash
mysql -u USER -p NOM_BDD < docs/refonte/migration_pilot.sql
```

## Critères de fin

- Au moins une journée documentée (OK ou problèmes notés)
- Parcours cycle sans bloqueur 500
- Décision go merge ou liste de correctifs
