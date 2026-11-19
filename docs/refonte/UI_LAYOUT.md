# Standard UI — conteneurs & en-têtes

## Shell de page

### Listes, tableaux, dashboards
```html
<body class="d-flex flex-column min-vh-100">
<nav th:replace="~{fragments/layout :: navbar}"></nav>
<div th:replace="~{fragments/layout :: flash-messages}"></div>
<main class="container-fluid px-3 px-md-4 py-4 flex-grow-1">
  <div class="page-header">...</div>
  ...
</main>
<div th:replace="~{fragments/layout :: footer}"></div>
<div th:replace="~{fragments/layout :: scripts}"></div>
</body>
```

### Formulaires courts
```html
<main class="container py-4 flex-grow-1 page-shell-narrow">
  <div class="page-header">...</div>
  <div class="card">...</div>
</main>
```

## En-tête `.page-header`

```html
<div class="page-header">
  <div>
    <h1 class="h4 fw-bold mb-0"><i class="fas fa-... me-2"></i>Titre</h1>
    <p class="text-muted small mb-0">Sous-titre optionnel</p>
  </div>
  <div class="d-flex flex-wrap gap-2"><!-- actions --></div>
</div>
```

## Pied de page

```html
<div th:replace="~{fragments/layout :: footer}"></div>
<div th:replace="~{fragments/layout :: scripts}"></div>
```
Une seule fois chaque. Ne pas doubler.

## Script local

```bash
python3 scripts/harmonize-layout.py
# ou
python3 scripts/harmonize-layout.py src/main/resources/templates
```

## Déjà alignés (exemples)

- `cycle/index.html`, `cycle` (plusieurs écrans)
- `commandes/non-traitees.html`, `commandes/edit.html`
- `admin/generateur-licence.html`
- `matiere-premerie/list.html` (référence)
- CSS : `page-shell-narrow`, padding unifié

## À faire en local si besoin

Relancer le script après `git pull`, puis vérifier manuellement les formulaires complexes (création commande, POS).
