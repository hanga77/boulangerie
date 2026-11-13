# Revue formulaires — Priorité 2 (Commandes, stocks, produits)

Branche : `refonte/phase-1`

## Périmètre

| Zone | Templates |
|------|-----------|
| Commandes | `commandes/create`, `edit` |
| Matières / stock | `matiere-premerie/*` (create, edit, add-stock, remove-stock, gestion) |
| Produits | `produits/create`, `edit`, `list` (delete) |

## Résultats

### Commandes

| Formulaire | CSRF | required | Notes |
|------------|------|----------|-------|
| create | ✅ | date, PDV, lignes | OK |
| edit | ✅ | date, PDV | quantités min=0 OK |

### Matières / mouvements

| Formulaire | CSRF avant | CSRF après FIX | Champs clés |
|------------|------------|----------------|-------------|
| create | ❌ | ✅ | nom, unité, stock min |
| edit | ❌ | ✅ | idem |
| add-stock | ❌ | ✅ | qté, prix, avaries |
| remove-stock | ❌ | ✅ | qté |
| gestion (mouvements) | ❌ | ✅ | type, qté, production liée pour SORTIE |

### Produits

| Formulaire | CSRF avant | CSRF après FIX |
|------------|------------|----------------|
| create | ❌ | ✅ |
| edit | ❌ | ✅ |
| delete (list) | ✅ | ✅ |

## Gravité

| Niveau | Détail |
|--------|--------|
| **Important** | Absence de token CSRF sur POST matières et produits |
| Mineur | Lignes dynamiques produit/matière (souvent JS) |

## Correctifs FORMS-P2-FIX

Ajout du token CSRF Spring sur tous les POST listés.

## Suite P3

Guichet POS, bulletins, employés, fournisseurs, admin.
