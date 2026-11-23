# Unification des interfaces

**Branche :** `refonte/phase-1`

## Lots

| Lot | Contenu | Statut |
|-----|---------|--------|
| U0 | Design system + menu Finance | ✅ |
| U1 | Login, register, 403/404/500 | ✅ |
| U2 | Commandes, produits, matières | ✅ |
| U3 | Finances, compta, fournisseurs | ✅ |
| U4 | Production / livraisons V1 | ✅ |
| U5 | Admin, employés, bulletins, guichet | ✅ |

## U5 livré

- `admin/inactive-users` (roles complets + CSRF + layout)
- `employes/list`, `employes/form`
- `bulletins/mes-bulletins`
- `guichet/select` (+ lien Caisse V2)

Les écrans admin users/settings/licence et guichet POS déjà structurés n'ont pas été réécrits en profondeur (risque métier).

## Convention globale

- `fragments/layout` : head, navbar, flash, scripts
- `page-header`, `card`, `table-sm`, `empty-state`
- Bandeaux `legacy-banner` sur modules V1
