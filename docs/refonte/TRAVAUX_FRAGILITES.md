# Travaux anti-fragilités

Branche : `refonte/phase-1`

## Lot F1 (démarré)

| Action | Statut |
|--------|--------|
| Service `OperationalReadinessService` | ✅ |
| Alertes dashboard (Compte Principal, double flux V1+V2) | ✅ |
| CTA Cycle V2 / création compte | ✅ |
| Consigne documentée « un seul flux » | ✅ |

## Lots suivants

| Lot | Contenu |
|-----|--------|
| F2 | Bandeaux legacy renforcés sur **toutes** pages production/livraisons V1 |
| F3 | Bouton « Créer Compte Principal » en 1 clic (ADMIN) si absent |
| F4 | Smoke test / pilote (`/admin/pilot`) sur 1 journée réelle |
| F5 | Merge `refonte/phase-1` → `master` après F4 |

## Consigne équipe

1. Journée courante = **Cycle du jour** uniquement.  
2. Menu **Historique** = consultation / secours, pas le flux du jour.  
3. Caisse recommandée = **Caisse V2** (`/cycle/caisse`).  
4. Avant prod : migrations V2 + `migration_pilot.sql` + Compte Principal.
