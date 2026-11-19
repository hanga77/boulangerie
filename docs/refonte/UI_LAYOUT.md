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

## Pied de page (anti-triple)

**Un seul** appel :
```html
<div th:replace="~{fragments/layout :: scripts}"></div>
```
Le fragment `scripts` inclut le footer + JS.

**Ne pas** combiner `:: footer` **et** `:: scripts` (sinon double pied de page).

## À éviter

| Ancien | Nouveau |
|--------|--------|
| `<div class="container mt-4">` | `<main class="container-fluid px-3 px-md-4 py-4 flex-grow-1">` |
| `h1` nu + boutons flottants | `.page-header` |
| `footer` + `scripts` (avec footer) | `scripts` seul |
