# Revue formulaires — Priorité 4 (résiduels + audit global)

Branche : `refonte/phase-1`

## Objectif

Balayage de **tous** les templates HTML : POST sans token CSRF, puis correctifs.

## Audit (107 templates)

| Statut | Nb |
|--------|-----|
| POST avec CSRF | ~50 |
| POST sans CSRF (avant P4) | charges-fixes, factures, compte, livraisons/create, register (re-check), error (forms admin) |

## Correctifs FORMS-P4

| Template | Action |
|----------|--------|
| `register.html` | CSRF (re-confirmé) |
| `finances/compte.html` | CSRF |
| `comptabilite/charges-fixes.html` | CSRF |
| `comptabilite/charges-fixes/liste.html` | CSRF |
| `comptabilite/factures.html` | CSRF |
| `livraisons/create.html` | CSRF |
| `error.html` (forms activation users) | CSRF sur 3 POST |

## Déjà OK (vérifié P4)

- `login.html` — CSRF présent
- Guichet `/guichet/vente` — garde « au moins un produit » côté serveur déjà en place
- Admin users / inactive-users — CSRF déjà présent

## Synthèse P1–P4

| Lot | Focus | Statut |
|-----|--------|--------|
| P1 | Cycle V2, caisse, magasin | ✅ |
| P2 | Commandes, stocks, produits | ✅ |
| P3 | Auth, POS, RH, fournisseurs, admin | ✅ |
| P4 | Résiduels compta / livraisons V1 / audit | ✅ |

## Suite optionnelle

- Job CI : échouer si `method="post"` sans `_csrf` dans `templates/`
- Smoke test manuel login → register → charges-fixes → livraison V1
