# Roadmap refonte Gestiboul

## Branches

| Branche | Rôle |
|---------|------|
| `master` | Base stable pré-V2 (en retard) |
| `refonte/phase-1` | Travail complet : phases 1–10 + UI |

Flux : **refonte → master** après checklist A + smoke B.

## Phases code

| Phase | Contenu | Statut |
|-------|---------|--------|
| 1–5 | Cycle V2, ventes, synthèse, tests, notes | ✅ |
| 6 | Trésorerie, KPI, dépenses/recettes, marge | ✅ |
| UI U0–U5 | Design system + écrans | ✅ |
| **7** | Pilote terrain (guide admin) | ✅ socle |
| **8** | Export CSV reporting | ✅ socle |
| **9** | Multi-comptes (virement UI) | ✅ socle |
| **10** | Health + webhook stub | ✅ socle |

Détail 7–10 : [PHASE7_10.md](./PHASE7_10.md)

## Suite terrain

1. Checklist technique A
2. Smoke test B (1 jour métier)
3. Merge PR → master
4. Déploiement
5. Itérations pilote (retours réels → correctifs)
