# Travaux anti-fragilités

Branche : `refonte/phase-1`

## Lot F1 — readiness dashboard ✅

## Lot F2 — bandeaux legacy V1 ✅

## Lot F3 — Compte Principal 1 clic ✅

| Élément | Détail |
|---------|--------|
| Service | `FinanceService.assurerComptePrincipal(solde)` |
| Endpoint | `POST /compte-bancaire/assurer-principal` (ADMIN) |
| Param | `soldeInitial` (défaut 0), `redirect` optionnel |
| UI | Dashboard (formulaire si absent) + page comptes |

Idempotent : ne recrée pas si le compte existe déjà.

## Lots suivants

| Lot | Contenu |
|-----|--------|
| F4 | Pilote réel `/admin/pilot` |
| F5 | Merge → `master` |

## Consigne équipe

1. Journée = **Cycle du jour** uniquement.  
2. Historique = consultation / secours.  
3. Caisse = **Caisse V2**.  
4. Avant prod : migrations + **Compte Principal** (bouton 1 clic ADMIN).
