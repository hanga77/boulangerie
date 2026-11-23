# Revue formulaires — Priorité 1 (Cycle, caisse, magasin)

Branche : `refonte/phase-1`  
Date : revue documentaire + lecture templates/contrôleurs.

## Périmètre P1

| Formulaire | Template | POST | CSRF |
|------------|----------|------|------|
| Lancer production | `cycle/lancer.html` | `/cycle/lancer` | ✅ |
| Confirmer sorties magasin | `cycle/feuille-sortie.html` | `/cycle/feuille/{id}/confirmer` | ✅ |
| Valider quantités | `cycle/valider.html` | `/cycle/valider/{id}` | ✅ |
| Livraison V2 | `cycle/livraison.html` | `/cycle/livraison/{id}` | ✅ |
| Caisse V2 | `cycle/caisse.html` | `/cycle/caisse/{id}` | ✅ |
| Dépenses | `cycle/depenses.html` | `/cycle/depenses` | ✅ |
| Recettes | `cycle/recettes.html` | `/cycle/recettes` | ✅ |
| Filtres sorties (GET) | `cycle/sorties.html` | — | N/A |

---

## Résultats par formulaire

### 1. Lancer production (`/cycle/lancer`)

| Critère | Statut | Commentaire |
|---------|--------|-------------|
| CSRF | ✅ | |
| Date requise | ✅ | `required` |
| Farinage matière | ⚠️ | Optionnel — OK métier |
| Farinage quantité | ⚠️ | Pas de lien « si matière choisie → qté obligatoire » (HTML) |
| Rôles | ✅ | ADMIN, MANAGER, BOULANGER |
| Erreurs serveur | ✅ | flash `errorMessage` |

**Reco :** JS léger : si `farinageMatiereId` renseigné, exiger `farinageQuantite > 0`.

---

### 2. Feuille / sorties magasin (`/cycle/feuille/{id}/confirmer`)

| Critère | Statut | Commentaire |
|---------|--------|-------------|
| CSRF | ✅ | |
| Quantités | ✅ | Map `qte[id]` ; vide = théorique entier (côté serveur) |
| Rôles | ✅ | ADMIN, MANAGER, MAGASINIER |
| UX | ✅ | Aligné besoin « quantités connues + farinage à la sortie » |

**Reco :** afficher clairement « laisser vide = théorique » sous chaque ligne (si pas déjà).

---

### 3. Valider quantités (`/cycle/valider/{id}`)

| Critère | Statut | Commentaire |
|---------|--------|-------------|
| CSRF | ✅ | |
| Produits / matières | ⚠️ | Champs non `required` ; saisie partielle possible |
| min=0 | ✅ | |
| Rôles | ✅ | |

**Reco :** exiger au moins une qté produit > 0 côté serveur (message clair) ; préremplir avec théorique si disponible.

---

### 4. Livraison V2

| Critère | Statut | Commentaire |
|---------|--------|-------------|
| CSRF | ✅ | |
| Client | ✅ | `nomClient` required |
| Point de vente | ⚠️ | Optionnel |
| Quantités | ⚠️ | `min=0` + `max=stock` HTML ; **pas** d’obligation « au moins 1 ligne > 0 » en HTML |
| Prix | ✅ | éditables |
| Revenu auto | ✅ | checkbox |

**Risque :** soumission avec toutes quantités à 0 → dépend du use case serveur.

**Reco :** contrôle JS + serveur : refuser total quantités = 0.

---

### 5. Caisse V2

| Critère | Statut | Commentaire |
|---------|--------|-------------|
| CSRF | ✅ | |
| Moyen paiement | ✅ | select |
| Quantités | ⚠️ | même risque qté toutes à 0 |
| max stock | ✅ | `th:max` |
| Confirm dialog | ✅ | `confirm('Enregistrer…')` |
| Bouton désactivé si vide | ✅ | `th:disabled="${restants.isEmpty()}"` |

**Reco :** avant submit, vérifier somme des `qte[*] > 0` (JS).

---

### 6. Dépenses / Recettes

| Critère | Statut | Commentaire |
|---------|--------|-------------|
| CSRF | ✅ | |
| Type, montant, description | ✅ | `required`, montant `min=1` |
| Compte | ✅ | hidden `compteId` si Compte Principal |
| Blocage sans compte | ✅ | form conditionné `compteId != null` |

**OK** pour P1 finance saisie.

---

## Synthèse P1

| Gravité | Nb | Thèmes |
|---------|-----|--------|
| Bloquant | 0 | — |
| Important | 3 | qté 0 en livraison/caisse ; farinage matière sans qté ; validation partielle |
| Mineur | 2 | libellés « vide = théorique » ; PDV optionnel |

**Verdict P1 :** formulaires **utilisables et sécurisés (CSRF, rôles)** ; pas de trou critique CSRF.  
Améliorations prioritaires = **gardes quantités > 0** (livraison + caisse) et **cohérence farinage**.

---

## Suite

- **P2** : commandes, matières/mouvements stock, produits  
- **P3** : guichet POS, bulletins, employés, fournisseurs, admin  

Corrections code des points « Important » : lot **FORMS-P1-FIX** (optionnel après validation métier).
