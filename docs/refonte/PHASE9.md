# Phase 9 — Multi-comptes & virements

## Livré

| Élément | Détail |
|---------|--------|
| Liste des comptes | `/compte-bancaire` |
| Création compte | `/compte-bancaire/creer` (ADMIN) |
| Virement | `POST /compte-bancaire/transfert` |
| Historique virements | Filtre dates sur la liste |
| Détail compte | `/compte-bancaire/{id}` + mouvements période |

## Règles métier

- Source ≠ destination
- Montant &gt; 0
- Solde source suffisant (`FinanceService.transfertEntreCcomptes`)
- Journal : `TRANSFERT_DEBIT` + `TRANSFERT_CREDIT` (hors CA / hors dépenses d’exploitation)

## Prérequis

Au moins **2 comptes** (ex. Compte Principal + Caisse).

## Menu

Finance → Comptes bancaires (si présent dans la navbar)
