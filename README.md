# Gestion Boulangerie

Application web de gestion complète pour boulangerie : production, stock, commandes, livraisons, ventes, comptabilité, RH.

**Stack :** Spring Boot 3.4.2 · Thymeleaf · MySQL 8 · Spring Security · Flying Saucer (PDF) · Java 17

---

## Démarrage rapide

### Prérequis

- Java 17+
- MySQL 8 en cours d'exécution sur `localhost:3306`
- Maven (ou utiliser `./mvnw`)

### Lancer l'application

```bash
# Avec le mot de passe MySQL par défaut (vide)
./mvnw spring-boot:run

# Avec identifiants MySQL personnalisés
DB_USERNAME=root DB_PASSWORD=monmotdepasse ./mvnw spring-boot:run
```

L'application démarre sur **http://localhost:9000**

La base de données `boulangerie_bd` est créée automatiquement au premier démarrage.
Les données de démonstration (utilisateurs, produits, fournisseurs, etc.) sont injectées automatiquement.

### Build JAR exécutable

```bash
./mvnw package -DskipTests
java -jar target/gestion-boulangerie-0.0.1-SNAPSHOT.jar
```

---

## Comptes utilisateurs (données de démonstration)

| Profil | Nom d'utilisateur | Mot de passe | Rôle |
|--------|------------------|--------------|------|
| Administrateur | `xavier` | `admin123` | ADMIN |
| Responsable | `manager` | `manager123` | MANAGER |
| Boulanger | `boulanger1` | `pain2024` | BOULANGER |
| Boulanger | `boulanger2` | `pain2024` | BOULANGER |
| Magasinier | `magasinier` | `stock2024` | MAGASINIER |

> Ces comptes sont créés automatiquement si la base est vide. Ne jamais utiliser ces mots de passe en production.

---

## Profils et permissions

### ADMIN — `xavier`

Accès total à toutes les fonctionnalités.

| Module | Permissions |
|--------|-------------|
| **Utilisateurs** | Créer, modifier, supprimer, changer le mot de passe, activer/désactiver |
| **Points de vente & guichets** | Créer, modifier, supprimer |
| **Produits** | Créer, modifier, supprimer |
| **Matières premières** | Gérer le stock, achats, ajustements |
| **Commandes** | Créer, modifier, supprimer, calculer matières nécessaires |
| **Production** | Voir l'historique, gérer les incidents, supprimer |
| **Livraisons** | Créer, imprimer facture PDF, enregistrer revenu |
| **Ventes libres** | Créer, consulter |
| **Finances** | Voir toutes les transactions, comptes bancaires, rapport mensuel |
| **Charges fixes** | Créer, payer, télécharger reçu PDF |
| **Facturation** | Créer factures, marquer payées |
| **Fournisseurs** | Gérer fournisseurs, dettes, remboursements |
| **Employés** | Créer, modifier, gérer les bulletins de paie, payer salaires |
| **Bulletins de paie** | Créer, payer, télécharger PDF (tous les employés) |
| **Rapports** | Accès à tous les rapports et KPIs |
| **Paramètres** | Seuil d'incident production, logo de l'application |

---

### MANAGER — `manager`

Gestion opérationnelle quotidienne. Pas d'accès à l'administration système.

| Module | Permissions |
|--------|-------------|
| **Utilisateurs** | Aucune (lecture seule de son propre profil) |
| **Points de vente & guichets** | Consultation uniquement |
| **Produits** | Créer, modifier, supprimer |
| **Matières premières** | Gérer le stock, achats, ajustements |
| **Commandes** | Créer, modifier, supprimer, calculer matières nécessaires |
| **Production** | Voir l'historique, gérer les incidents |
| **Livraisons** | Créer, imprimer facture PDF, enregistrer revenu |
| **Ventes libres** | Créer, consulter |
| **Finances** | Voir toutes les transactions, comptes bancaires, rapport mensuel |
| **Charges fixes** | Créer, payer, télécharger reçu PDF |
| **Facturation** | Créer factures, marquer payées |
| **Fournisseurs** | Gérer fournisseurs, dettes, remboursements |
| **Employés** | Créer, modifier, gérer les bulletins de paie, payer salaires |
| **Bulletins de paie** | Créer, payer, télécharger PDF (tous les employés) |
| **Rapports** | Accès à tous les rapports et KPIs |
| **Paramètres** | Aucun accès |

---

### BOULANGER — `boulanger1` / `boulanger2`

Accès limité aux opérations de production.

| Module | Permissions |
|--------|-------------|
| **Commandes** | Consulter la liste et le détail (lecture seule) |
| **Production** | Lancer la production du jour, saisir les quantités réelles, signaler un incident |
| **Bulletin de paie** | Consulter et télécharger son propre bulletin PDF |
| **Tout le reste** | Accès refusé (403) |

**Scénario typique boulanger :**
1. Se connecter → Dashboard
2. Voir les commandes en attente → `/commandes`
3. Démarrer la production → `/production/passer-a-la-production`
4. Saisir les quantités réelles produites → formulaire de validation
5. Signaler un incident si les quantités réelles < théoriques
6. Consulter son bulletin de paie → `/bulletins/mes-bulletins`

---

### MAGASINIER — `magasinier`

Accès exclusif à la gestion du stock des matières premières.

| Module | Permissions |
|--------|-------------|
| **Matières premières** | Consulter les niveaux de stock, enregistrer des mouvements |
| **Mouvements de stock** | ENTREE (réception fournisseur), SORTIE (liée à une production obligatoire), RETOUR, PERTE |
| **Bulletin de paie** | Consulter et télécharger son propre bulletin PDF |
| **Tout le reste** | Accès refusé (403) |

**Règle clé :** toute **SORTIE** de matière première doit être liée à une production existante — garantit la traçabilité stock ↔ production.

**Scénario typique magasinier :**
1. Se connecter → Dashboard
2. Aller dans Gestion → Mouvements Stock → `/matieres-premieres/mouvements-stock`
3. Enregistrer une **ENTREE** lors de la réception d'un fournisseur (prix unitaire obligatoire → Transaction ACHAT)
4. Enregistrer une **SORTIE** en sélectionnant la production de référence dans le menu déroulant
5. Enregistrer un **RETOUR** si une matière non utilisée revient au stock (motif optionnel)
6. Enregistrer une **PERTE** pour casse ou péremption (motif obligatoire)

---

## Flux métier principaux

### Cycle journalier complet

```
Manager crée commandes (points de vente)
    ↓
Boulanger lance la production
    ↓
Production calcule les matières nécessaires et débite le stock
    ↓
Boulanger valide avec quantités réelles (+ incident si écart)
    ↓
Manager crée livraisons depuis la production du jour
    ↓
Manager enregistre le revenu de la livraison (→ Transaction VENTE)
```

### Gestion des charges fixes

```
Admin/Manager crée une charge fixe (loyer, électricité…)
    ↓
À l'échéance : formulaire de paiement (choix du compte bancaire)
    ↓
Transaction CHARGE_FIXE créée, solde débité
    ↓
Reçu PDF téléchargeable
    ↓
Nouvelle échéance créée automatiquement selon la périodicité
   (MENSUEL / TRIMESTRIEL / SEMESTRIEL / ANNUEL)
```

### Gestion des stocks matières premières (magasin)

> Ce flux est **indépendant de la production**. Il couvre les achats fournisseurs,
> les pertes/ajustements manuels et les retours — sans déclencher le cycle de production.

```
Admin/Manager va dans Matières premières → Gestion des mouvements
    ↓
Choisit la matière et le type de mouvement :
    ENTREE  — réception fournisseur (prix unitaire obligatoire → Transaction ACHAT)
    SORTIE  — perte, casse, consommation hors-production
    RETOUR  — surplus retourné en stock

Stock mis à jour immédiatement
    ↓
Mouvement tracé dans l'historique (/matieres-premieres/mouvements-stock)
```

**Alertes automatiques** : si le stock descend sous le seuil minimum (`stockMinimum`),
un badge d'alerte s'affiche dans le dashboard.

---

### Paie des employés

```
Admin/Manager crée les bulletins de paie (période mensuelle)
    ↓
Calcul automatique : salaire brut → cotisations CNPS → salaire net
    (Employé 4,2% · Patronal 16,2%)
    ↓
Paiement via le Compte Principal (vérification de solde)
    ↓
Transaction SALAIRE créée
    ↓
Bulletin PDF téléchargeable par l'employé (ou par admin/manager)
```

---

## Application desktop installable ?

**À l'état actuel : non.** L'application est une application web qui nécessite :
- Un serveur Java en cours d'exécution
- Une instance MySQL séparée

**Deux options pour en faire un exécutable desktop :**

### Option A — JAR + MySQL local (simple, recommandé pour usage interne)
1. Installer MySQL une fois sur le poste
2. Distribuer le JAR + un script de démarrage `.bat` / `.sh`
3. L'utilisateur ouvre `http://localhost:9000` dans son navigateur

```bat
REM start.bat
start "" "http://localhost:9000"
java -jar gestion-boulangerie.jar
```

### Option B — Exécutable natif avec base embarquée (plus complexe)
Nécessite de modifier l'application :
1. Remplacer MySQL par **H2 en mode fichier** (base embarquée, zéro installation)
   ```properties
   spring.datasource.url=jdbc:h2:file:./boulangerie-data
   ```
2. Utiliser **`jpackage`** (inclus dans JDK 17) pour créer un `.exe` / `.msi` Windows avec le JRE bundlé
3. Résultat : installeur Windows autonome, aucune dépendance externe

> Option B implique des tests de migration des données MySQL → H2 et la validation de la compatibilité des requêtes JPA.

---

## Variables d'environnement

| Variable | Valeur par défaut | Description |
|----------|------------------|-------------|
| `DB_USERNAME` | `root` | Utilisateur MySQL |
| `DB_PASSWORD` | *(vide)* | Mot de passe MySQL |
| `MAIL_USERNAME` | `changeme@gmail.com` | Compte Gmail pour les emails |
| `MAIL_PASSWORD` | `changeme` | Mot de passe d'application Gmail |

---

## Structure des modules

```
src/main/java/.../
├── config/         DataInitializer, GlobalModelAttributes, AppSettings
├── controller/     Tous les contrôleurs web
├── dto/            Objets de transfert (formulaires, vues)
├── exception/      Gestion globale des erreurs
├── mapper/         Conversions Entity ↔ DTO
├── model/          Entités JPA
├── repository/     Interfaces Spring Data
├── security/       SecurityConfig, JWT, rate limiting
└── service/        Logique métier

src/main/resources/
├── static/
│   ├── css/        Bootstrap 5 + styles custom
│   ├── js/         scripts.js (remplacement Bootstrap JS, sans CDN)
│   └── fonts/      arial.ttf (pour les PDF Flying Saucer)
└── templates/      Templates Thymeleaf par module
```

---

## Tests

```bash
./mvnw test
```

81 tests unitaires couvrant les services principaux.
