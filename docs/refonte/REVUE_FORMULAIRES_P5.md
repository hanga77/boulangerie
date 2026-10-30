# Revue formulaires — Priorité 5 (garde-fou CI)

Branche : `refonte/phase-1`

## Objectif

Empêcher la **régression** : tout nouveau `<form method="post">` sans token CSRF fait **échouer la CI**.

## Livrables

| Fichier | Rôle |
|---------|------|
| `scripts/check-csrf-forms.sh` | Scan local / CI des templates |
| `.github/workflows/ci.yml` | Étape **Audit CSRF templates (FORMS-P5)** avant compile |

## Utilisation locale

```bash
chmod +x scripts/check-csrf-forms.sh
./scripts/check-csrf-forms.sh
# ou
./scripts/check-csrf-forms.sh src/main/resources/templates
```

## Règle

Pour chaque balise `<form … method="post">` hors commentaire HTML, les **15 lignes suivantes** doivent contenir `_csrf.token`.

## Synthèse série P1–P5

| Lot | Focus | Statut |
|-----|--------|--------|
| P1 | Cycle V2, caisse, magasin + gardes qté | ✅ |
| P2 | Commandes, stocks, produits + CSRF | ✅ |
| P3 | Auth, POS, RH, fournisseurs, admin | ✅ |
| P4 | Résiduels compta / livraisons V1 | ✅ |
| P5 | **CI anti-régression CSRF** | ✅ |

## Suite hors revue formulaires

- Ouvrir / merger la PR `refonte/phase-1` → `master`
- Pilote terrain `/admin/pilot`
- Smoke test métier journée complète
