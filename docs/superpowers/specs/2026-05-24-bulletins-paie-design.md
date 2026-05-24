# Design — Bulletins de paie employés

**Date :** 2026-05-24
**Projet :** Gestion Boulangerie (Spring Boot 3.4.2 / Thymeleaf / MySQL)
**Périmètre :** Sous-projet B — module RH : gestion des employés et génération des bulletins de paie

---

## 1. Contexte et objectif

Actuellement, les salaires sont enregistrés comme des `ChargeFixe` avec `type="SALAIRE"` et une description textuelle ("Salaire boulanger 1"). Il n'existe pas d'entité employé ni de bulletin de paie structuré.

**Objectifs :**
- Créer un registre d'employés (avec lien optionnel vers un compte User)
- Générer des bulletins de paie mensuels avec tous les composants salariaux
- Permettre le téléchargement PDF de chaque bulletin
- Intégrer le paiement dans le système financier existant via une `Transaction`
- Donner aux boulangers l'accès à leurs propres bulletins

---

## 2. Modèle de données

### 2.1 Entité `Employe`

```java
@Entity
public class Employe {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String poste; // ex. "Boulanger", "Manager"

    private double salaireBase;
    private LocalDate dateEmbauche;
    private String numeroCnps; // optionnel

    private boolean actif = true;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id")
    private User user; // lien optionnel vers compte système
}
```

### 2.2 Enum `StatutBulletin`

```java
public enum StatutBulletin {
    GENERE,   // calculé, non encore payé
    PAYE      // paiement effectué, Transaction liée
}
```

### 2.3 Entité `BulletinDePaie`

```java
@Entity
public class BulletinDePaie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @Column(nullable = false)
    private LocalDate periode; // premier jour du mois (ex. 2026-05-01)

    // Composantes
    private double salaireBase;         // capturé à la génération
    private double primes;              // primes diverses
    private double indemnitesTransport; // indemnité transport

    // Calculés (stockés en base)
    private double salaireBrut;         // salaireBase + primes + indemnitesTransport
    private double cnpsEmploye;         // 4,2% du brut
    private double cnpsPatronal;        // 16,2% du brut (informatif, à la charge de l'employeur)
    private double avanceSurSalaire;    // avance préalablement versée à déduire
    private double salaireNet;          // brut − cnpsEmploye − avanceSurSalaire

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutBulletin statut; // GENERE → PAYE

    @Column(nullable = false)
    private LocalDate dateGeneration;

    private LocalDate datePaiement; // null si statut = GENERE

    @OneToOne(optional = true)
    private Transaction transaction; // lien après paiement
}
```

**Règles de calcul :**
```
salaireBrut      = salaireBase + primes + indemnitesTransport
cnpsEmploye      = salaireBrut × 4,2 %
cnpsPatronal     = salaireBrut × 16,2 %   (affiché sur le bulletin, payé par l'employeur)
salaireNet       = salaireBrut − cnpsEmploye − avanceSurSalaire
```

**Contrainte d'unicité :** Un seul bulletin par employé par période (contrainte `@UniqueConstraint` sur `(employe_id, periode)`).

---

## 3. Composants backend

| Composant | Type | Rôle |
|---|---|---|
| `Employe` | Entity | Profil employé |
| `StatutBulletin` | Enum | GENERE / PAYE |
| `BulletinDePaie` | Entity | Bulletin mensuel |
| `EmployeRepository` | Repository | CRUD + `findByUser`, `findByActifTrue` |
| `BulletinDePaieRepository` | Repository | `findByEmploye`, `findByPeriodeBetween`, `findByStatut`, `findByEmployeAndPeriode` |
| `EmployeService` | Service | CRUD employés, `getEmployeByUser()` |
| `BulletinDePaieService` | Service | `genererBulletin()`, `payerBulletin()`, requêtes |
| `EmployeController` | Controller | Routes `/employes/**` |
| `BulletinDePaieController` | Controller | Routes `/bulletins/**` |
| `PdfController` | Modifié | Ajout route `GET /bulletins/{id}/pdf` |

---

## 4. Logique métier

### 4.1 `genererBulletin()`

```
Entrée : employeId, periode, primes, indemnitesTransport, avanceSurSalaire
1. Vérifier qu'aucun bulletin n'existe déjà pour (employe, periode) → exception si doublon
2. Récupérer l'employé
3. Calculer salaireBrut, cnpsEmploye, cnpsPatronal, salaireNet
4. Créer BulletinDePaie(statut=GENERE, dateGeneration=aujourd'hui)
5. Persister et retourner
```

### 4.2 `payerBulletin(id)`

```
1. Charger le bulletin ; vérifier statut = GENERE → exception si déjà PAYE
2. Récupérer CompteBancaire("Compte Principal")
3. Créer Transaction(
       type     = "SALAIRE",
       montant  = salaireNet,
       date     = aujourd'hui,
       description = "Salaire [prenom nom] — [mois/année periode]",
       compteBancaire = Compte Principal
   )
4. Déduire salaireNet du solde du compte
5. Sauvegarder Transaction + CompteBancaire
6. Mettre à jour bulletin : statut=PAYE, datePaiement=aujourd'hui, transaction=transaction
7. Retourner le bulletin mis à jour
```

---

## 5. Pages

### 5.1 `GET /employes`
- Accès : ADMIN, MANAGER
- Liste de tous les employés actifs (nom, prénom, poste, salaire de base, lien User)
- Boutons : Modifier, Nouveau bulletin, Désactiver

### 5.2 `GET/POST /employes/nouveau` et `GET/POST /employes/{id}/modifier`
- Accès : ADMIN, MANAGER
- Formulaire : nom, prénom, poste, salaire de base, date d'embauche, numéro CNPS, lien vers User (select optionnel)

### 5.3 `POST /employes/{id}/desactiver`
- Accès : ADMIN, MANAGER
- Désactivation logique (`actif = false`). Ne supprime pas les bulletins liés.

### 5.4 `GET /bulletins`
- Accès : ADMIN, MANAGER
- Filtres : période (mois/année), employé, statut (GENERE/PAYE/tous)
- KPI cards : total net à payer (bulletins GENERE), total payé ce mois, nombre de bulletins GENERE
- Tableau : employé, période, brut, net, statut badge, actions

### 5.5 `GET/POST /bulletins/generer`
- Accès : ADMIN, MANAGER
- Formulaire : select employé, mois/année, primes, indemnités transport, avance
- Affiche un résumé calculé en JavaScript avant soumission

### 5.6 `GET /bulletins/{id}`
- Accès : ADMIN, MANAGER, ou le BOULANGER dont l'User est lié à cet employé
- Vue complète : en-tête employé, tableau de toutes les composantes, statut, boutons PDF et Payer

### 5.7 `POST /bulletins/{id}/payer`
- Accès : ADMIN, MANAGER
- Déclenche `payerBulletin()`, redirige vers `/bulletins/{id}` avec message de succès

### 5.8 `GET /bulletins/{id}/pdf`
- Accès : ADMIN, MANAGER, ou BOULANGER lié à cet employé
- Génère et télécharge le PDF via Flying Saucer (template `employes/bulletin-template.html`)

### 5.9 `GET /bulletins/mes-bulletins`
- Accès : tous rôles authentifiés
- Récupère l'employé lié à l'utilisateur courant ; liste ses bulletins
- Si aucun employé lié : message "Aucun profil employé associé à votre compte"

---

## 6. Template PDF

Fichier : `src/main/resources/templates/employes/bulletin-template.html`

Structure du bulletin :
- En-tête : nom de la boulangerie, logo (si disponible), période
- Informations employé : nom, prénom, poste, N° CNPS
- Tableau des composantes :
  | Libellé | Montant |
  |---|---|
  | Salaire de base | X XAF |
  | Primes | X XAF |
  | Indemnité de transport | X XAF |
  | **Salaire brut** | **X XAF** |
  | Cotisation CNPS employé (4,2%) | − X XAF |
  | Avance sur salaire | − X XAF |
  | **Net à payer** | **X XAF** |
- Section informatif (grisée) : Cotisation patronale CNPS (16,2%) = X XAF
- Pied de page : date de paiement, signature employeur

---

## 7. Sécurité

| Route | Rôle requis |
|---|---|
| `GET/POST /employes/**` | ADMIN, MANAGER |
| `GET/POST /bulletins/**` | ADMIN, MANAGER |
| `GET /bulletins/{id}` | ADMIN, MANAGER **ou** BOULANGER lié |
| `GET /bulletins/{id}/pdf` | ADMIN, MANAGER **ou** BOULANGER lié |
| `GET /bulletins/mes-bulletins` | Tout utilisateur authentifié |
| `POST /bulletins/{id}/payer` | ADMIN, MANAGER |

---

## 8. Navigation

- Dropdown **Admin** dans la navbar : lien "Employés" → `/employes`
- Dropdown **Rapports** : lien "Bulletins de paie" → `/bulletins` (visible ADMIN/MANAGER)
- Dashboard (optionnel) : badge "X bulletins en attente de paiement"

---

## 9. Ce qui n'est PAS dans ce périmètre

- Calcul des congés payés ou indemnités de fin de service
- Import/export CSV de la masse salariale
- Workflow de validation à plusieurs niveaux (double approbation)
- Déclaration CNPS automatique vers un organisme externe
- Historique des modifications de salaire de base (`salaireBase` est éditable librement)
