# Travaux anti-fragilités

Branche : `refonte/phase-1`

## Lot F1 — readiness dashboard

| Action | Statut |
|--------|--------|
| Service `OperationalReadinessService` | ✅ |
| Alertes dashboard (Compte Principal, double flux V1+V2) | ✅ |
| CTA Cycle V2 | ✅ |

## Lot F2 — bandeaux legacy V1

| Page | Statut |
|------|--------|
| `livraisons/list.html` | déjà OK |
| `livraisons/today.html` | déjà OK |
| `livraisons/list-by-production.html` | déjà OK |
| `livraisons/confirmation.html` | ✅ ajouté |
| `production/passer-a-la-production.html` | déjà OK |
| `production/previous.html` | déjà OK |
| `production/details.html` | ✅ ajouté |
| `production/confirm.html` | ✅ ajouté |
| `production/incidents.html` | ✅ ajouté |
| `vente-libre/list.html` | déjà OK |
| `production/print.html` | volontairement sans bandeau (PDF print) |

Message type : module historique + CTA **Cycle du jour**.

## Lots suivants

| Lot | Contenu |
|-----|--------|
| F3 | Création Compte Principal en 1 clic (ADMIN) |
| F4 | Pilote réel `/admin/pilot` |
| F5 | Merge → `master` |

## Consigne équipe

1. Journée courante = **Cycle du jour** uniquement.
2. Menu **Historique** = consultation / secours.
3. Caisse recommandée = **Caisse V2** (`/cycle/caisse`).
4. Migrations V2 + `migration_pilot.sql` + Compte Principal avant prod.
