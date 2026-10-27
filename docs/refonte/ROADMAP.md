# Roadmap refonte Gestiboul

## Relation branches

| Branche | Rôle |
|---------|------|
| `master` | Ancienne base stable (pré-cycle V2) |
| `refonte/phase-1` | **Branche de travail complète** — contient master + phases 1–6 + UI |
| `main` | Présente dans le dépôt ; ne pas confondre avec master |

**Important :** il n’y a **rien à « corriger depuis master »** vers la refonte :
`refonte/phase-1` est **en avance** et inclut déjà l’historique de `master`.
Le sens du flux est : **refonte → master** (PR / merge), pas l’inverse.

## Phases livrées (code)

| Phase | Contenu | Statut |
|-------|---------|--------|
| 1 | Domaine production V2, API, farinage / magasin | ✅ |
| 2 | Livraisons / ventes V2, clôture | ✅ |
| 3–5 | Synthèse, tests, notes, non-régression V1 | ✅ |
| 6 | Trésorerie unifiée, KPI, dépenses/recettes, marge | ✅ |
| UI U0–U5 | Design system + écrans principaux | ✅ |

## Suite (pas une « phase 7 » métier obligatoire)

1. **A** Technique : backup, `migration_v2_all.sql`, secrets, Compte Principal  
   → `CHECKLIST_TECH_A.md`
2. **B** Smoke test cycle réel (un jour métier)  
   → `CHECKLIST_MERGE.md`
3. **Merge** PR `refonte/phase-1` → `master`
4. **Déploiement** prod  
   → `DEPLOIEMENT.md`

### Évolutions optionnelles (plus tard)

- Phase métier ciblée si besoin terrain (ex. WhatsApp, multi-sites)
- Affiner écrans restants (POS, settings) sans nouvelle architecture
- E2E automatisés au-delà des tests unitaires CI
