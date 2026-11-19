# Phases 7 à 10

Branche : `refonte/phase-1`

Ces phases prolongent la refonte **après** le cycle V2 et la finance unifiée (1–6).

| Phase | Objectif | Livrable |
|-------|---------|----------|
| **7** | Pilote terrain | Guide `/admin/pilot` + checklist opérationnelle |
| **8** | Reporting avancé | Export CSV CA consolidé `/cycle/reporting/export.csv` |
| **9** | Multi-comptes | UI virement entre comptes (API déjà en `FinanceService`) |
| **10** | Intégrations | Health public + webhook stub documenté |

## Phase 7 — Pilote

- URL : `/admin/pilot` (ADMIN)
- Objectif : dérouler 1 journée réelle avec le cycle V2 et noter les écarts
- Voir aussi `CHECKLIST_TECH_A.md` et `CHECKLIST_MERGE.md` section B

## Phase 8 — Export CSV

```
GET /cycle/reporting/export.csv?debut=YYYY-MM-DD&fin=YYYY-MM-DD
```

Colonnes : date, type, description, montant.  
Rôles : ADMIN, MANAGER.

## Phase 9 — Multi-comptes

- Liste : `/compte-bancaire`
- POST virement : `/compte-bancaire/transfert` (source, destination, montant, description)
- Journal : types `TRANSFERT_DEBIT` / `TRANSFERT_CREDIT` (hors CA / hors charges d’exploitation)

## Phase 10 — Intégrations

| Endpoint | Auth | Rôle |
|----------|------|------|
| `GET /api/public/health` | public | Monitoring / load balancer |
| `POST /api/integrations/webhook` | ADMIN | Stub journalisé (WhatsApp / ERP plus tard) |

WhatsApp Business et apps mobiles restent **hors scope** tant que le pilote n’est pas validé.
