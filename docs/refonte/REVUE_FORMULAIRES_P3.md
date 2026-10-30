# Revue formulaires — Priorité 3 (POS, RH, fournisseurs, admin, auth)

Branche : `refonte/phase-1`

## Périmètre

| Zone | Templates |
|------|-----------|
| Auth | `login.html`, `register.html` |
| Guichet | `guichet/pos.html`, `select.html` |
| Bulletins | `bulletins/generer.html`, `detail.html` |
| Employés | `employes/form.html`, `list.html` |
| Fournisseurs | `form`, `list`, `detail`, `dettes` |
| Admin | `users`, `settings`, `licence` |

## Résultats CSRF

| Formulaire | CSRF avant | Après FIX |
|------------|------------|-----------|
| **login** | ❌ | ✅ |
| **register** | ❌ | ✅ |
| Guichet POS | ✅ | — |
| Bulletins générer / détail | ✅ | — |
| Employés form / list | ✅ | — |
| Fournisseurs (tous) | ✅ | — |
| Admin users / settings / licence | ✅ | — |
| Générateur licence | pas de POST form | — |

Spring Security **n’a pas** désactivé CSRF → login/register sans token = risque de **403 Forbidden** à la connexion.

## Qualité métier (échantillon)

| Formulaire | Points OK | Points d’attention |
|------------|-----------|---------------------|
| Guichet POS | CSRF, productionId, moyen paiement | Vérifier au moins 1 qté > 0 côté serveur (comme caisse V2) |
| Bulletins | employé required, périodicité | Cohérence mois/semaine selon radio |
| Fournisseurs dette/remboursement | min montant, max dette | — |
| Employés | CSRF | Champs salaire / poste à valider métier |
| Admin users | CSRF multi-forms | — |

## Correctifs FORMS-P3-FIX

- Token CSRF sur `login.html` et `register.html`
- `autocomplete` username/password sur auth
- `minlength="6"` sur mot de passe inscription

## Synthèse P1+P2+P3

| Priorité | Focus | Statut |
|----------|--------|--------|
| P1 | Cycle, caisse, magasin | ✅ + gardes qté |
| P2 | Commandes, stocks, produits | ✅ CSRF matières/produits |
| P3 | Auth, POS, RH, fournisseurs, admin | ✅ CSRF auth |

Les formulaires **critiques** (auth + cycle + stock + produits) ont désormais le token CSRF et des gardes métier sur les ventes/livraisons V2.

## Suite optionnelle

- Garde « au moins 1 qté » sur `/guichet/vente` (alignée caisse V2)
- Tests manuels login → cycle → stock → POS
- Audit automatisé : grep `method="post"` sans `_csrf`
