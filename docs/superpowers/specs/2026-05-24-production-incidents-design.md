# Design — Production : comparatif quantités et gestion des incidents

**Date :** 2026-05-24
**Projet :** Gestion Boulangerie (Spring Boot 3.4.2 / Thymeleaf / MySQL)
**Périmètre :** Sous-projet A — amélioration du flux de production

---

## 1. Contexte et objectif

Le flux de production actuel permet au boulanger de saisir les quantités réelles utilisées lors de la validation, mais n'offre pas de comparaison visuelle avec les quantités théoriques, et ne permet pas de tracer les avaries (fournée brûlée, pâte ratée, matière avariée, etc.).

**Objectifs :**
- Afficher en temps réel le comparatif théorique vs réel pendant la saisie
- Détecter automatiquement les écarts dépassant un seuil configurable
- Permettre au boulanger de signaler un incident sans friction supplémentaire
- Appliquer automatiquement l'impact stock selon le type d'incident
- Offrir un historique consultable des incidents au responsable

---

## 2. Modèle de données

### 2.1 Nouvelle entité : `IncidentProduction`

```java
@Entity
public class IncidentProduction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "production_id")
    private Production production;

    @Enumerated(EnumType.STRING)
    private TypeIncident type; // PATE_RATEE, FOURNEE_BRULEE, MATIERE_AVARIEE,
                               // PANNE_FOUR, SOUS_RENDEMENT, AUTRE

    @ManyToOne(optional = true) @JoinColumn(name = "produit_id")
    private Produit produitConcerne;

    @ManyToOne(optional = true) @JoinColumn(name = "matiere_id")
    private MatierePremiere matiereConcernee;

    private double quantitePerdue;
    private String cause;          // description libre
    private LocalDate dateIncident; // = date de la production
    private boolean stockAjuste;   // true si StockMovement PERTE créé

    @ManyToOne @JoinColumn(name = "signaled_by")
    private User signaledBy;

    @OneToOne(optional = true)
    private StockMovement mouvementStock; // null si stockAjuste = false
}
```

**Règle stock par type :**
| Type | Déduction stock automatique |
|---|---|
| PATE_RATEE | Oui — StockMovement PERTE pour chaque matière utilisée |
| MATIERE_AVARIEE | Oui — StockMovement PERTE sur la matière concernée |
| FOURNEE_BRULEE | Non — matières déjà comptées dans la validation |
| PANNE_FOUR | Non |
| SOUS_RENDEMENT | Non |
| AUTRE | Non |

### 2.2 Nouvelle entité : `AppSettings`

```java
@Entity
public class AppSettings {
    @Id
    private String cle;   // ex. "seuil_incident_production"
    private String valeur; // ex. "10.0"
}
```

Valeur par défaut au démarrage : `seuil_incident_production = 10.0` (%)

---

## 3. Flux de validation production

### 3.1 Page `production/confirm.html` (modifiée)

Tableau comparatif pour les **matières premières** :

| Matière | Unité | Théorique | Réel (saisie) | Écart % |
|---|---|---|---|---|
| Farine de blé | kg | 8.0 | [input] | calculé en JS |

Tableau comparatif pour les **produits** :

| Produit | Prévu | Produit réel | Écart % |
|---|---|---|---|
| Pain ordinaire | 200 | [input] | calculé en JS |

**Comportement JavaScript :**
- Écart % = `Math.abs(réel - théorique) / théorique * 100`
- Si écart % > seuil (injecté depuis le serveur via `th:data-seuil`) :
  - La ligne passe en rouge (`table-danger`)
  - La section "Signaler un incident" apparaît en bas du formulaire
- Section incident : type (select), élément concerné (texte auto-rempli), quantité perdue (number), cause (textarea)
- La section incident est **optionnelle** : le boulanger peut valider sans la remplir

### 3.2 `POST /production/valider-production` (modifié)

Séquence après mise à jour des quantités réelles (existant) :

```
Pour chaque produit/matière avec incident soumis dans le formulaire :
  1. Créer IncidentProduction (type, quantitePerdue, cause, signaledBy, dateIncident)
  2. Si type IN (PATE_RATEE, MATIERE_AVARIEE) :
       Créer StockMovement(type=PERTE, matiere, quantite=quantitePerdue, motif=cause)
       Mettre à jour stock matière -= quantitePerdue
       Lier mouvementStock à l'incident
       stockAjuste = true
  3. Sinon : stockAjuste = false
```

---

## 4. Pages

### 4.1 `production/details.html` (modifiée)

Ajout d'une section **"Incidents"** en bas de la page détail production :
- Si aucun incident : message "Aucun incident signalé pour cette production"
- Sinon : tableau (type, produit/matière, perte, cause, signalé par, stock ajusté ✓/✗)

### 4.2 `production/incidents.html` (nouvelle)

URL : `GET /production/incidents`
Accès : ADMIN, MANAGER

Contenu :
- Filtres : période (début/fin), type d'incident, boulanger
- 3 KPI cards : nombre total d'incidents, pertes avec impact stock, pertes sans impact stock
- Tableau : date, production, type (badge coloré), produit/matière, quantité perdue, cause, boulanger

### 4.3 `admin/settings.html` (modifiée)

Ajout d'un champ :
```
Seuil d'alerte incident production (%)
[10] %
[Enregistrer]
```

---

## 5. Composants backend

| Composant | Type | Rôle |
|---|---|---|
| `IncidentProduction` | Entity | Modèle incident |
| `TypeIncident` | Enum | 6 types d'incident |
| `AppSettings` | Entity | Paramètres clé/valeur |
| `IncidentProductionRepository` | Repository | CRUD + findByProductionId, findByType, findByDateBetween |
| `AppSettingsRepository` | Repository | findByCle |
| `IncidentProductionService` | Service | creerIncident(), getIncidentsByProduction(), getAllIncidents() |
| `AppSettingsService` | Service | getSeuil(), updateSeuil() |
| `ProductionController` | Modifié | valider-production traite les incidents, injecte seuil dans le modèle |
| `UserController` | Modifié | POST /admin/settings/seuil-incident |

---

## 6. Sécurité

| Route | Rôle requis |
|---|---|
| `POST /production/valider-production` | BOULANGER (existant) |
| `GET /production/incidents` | ADMIN, MANAGER |
| `POST /admin/settings/seuil-incident` | ADMIN |

---

## 7. Ce qui n'est PAS dans ce périmètre

- Notification push/email lors d'un incident
- Tableau de bord incidents sur le dashboard principal
- Calcul de coût financier des pertes (reporté au module comptabilité)
- Incidents liés aux livraisons ou aux ventes libres
