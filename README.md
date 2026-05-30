# Gestiboul — Gestion complète de boulangerie

**Gestiboul** est une application web de gestion tout-en-un conçue pour les boulangeries artisanales et industrielles. Elle couvre l'intégralité du cycle opérationnel : commandes clients, production, gestion des stocks, livraisons, ventes, comptabilité, paie des employés et suivi des fournisseurs.

**Stack :** Spring Boot 3.4.2 · Java 17 · Thymeleaf · MySQL 8 · Spring Security · Flying Saucer (PDF)

---

## Fonctionnalités

### Commandes & Production
- Création de commandes par point de vente avec quantités par produit
- Lancement de la production depuis les commandes du jour : calcul automatique des matières premières nécessaires et débit du stock
- Saisie des quantités réellement produites par les boulangers
- Comparaison tripartite : quantité de la recette / donnée par le magasinier / utilisée par le boulanger
- Détection automatique des incidents de production si les écarts dépassent un seuil configurable

### Stock des matières premières
- Quatre types de mouvements : ENTRÉE (réception fournisseur), SORTIE (liée à une production), RETOUR, PERTE
- À la réception : saisie de la quantité commandée et des avaries — la quantité nette est calculée automatiquement
- Prix unitaire obligatoire à l'entrée → Transaction ACHAT générée automatiquement
- Alertes de stock bas dans le tableau de bord
- Historique complet avec traçabilité (utilisateur, motif, production liée)

### Livraisons & Ventes
- Création de livraisons depuis les productions du jour ou de la veille
- Chargement AJAX des produits disponibles selon la production sélectionnée
- Génération automatique d'une facture PDF à chaque livraison
- Ventes libres aux guichets directement depuis les stocks produits
- Enregistrement du revenu → Transaction VENTE créditée sur le compte principal

### Finances & Comptabilité
- Tableau de bord financier : soldes de comptes, flux mensuels, résultat
- Charges fixes (loyer, électricité…) avec périodicité et renouvellement automatique
- Paiement des charges avec choix du compte bancaire et reçu PDF téléchargeable
- Facturation clients avec suivi des paiements
- Rapport mensuel des transactions par catégorie

### Gestion des fournisseurs
- Annuaire des fournisseurs avec contacts
- Enregistrement des dettes (achat à crédit ou impayé)
- Remboursements partiels ou totaux avec historique
- Vue globale des dettes en cours avec détection des retards

### Guichet POS (caissier)
- Interface caisse tactile : tuiles +/− par produit, total mis à jour en temps réel
- Chargement automatique des produits restants de la production du jour (ou veille)
- Choix du moyen de paiement : Espèces ou Mobile Money (Orange Money, MTN)
- Impression ticket thermique après encaissement via `window.print()` CSS @media print
- Largeur ticket configurable (58mm ou 80mm) dans les Paramètres admin
- Séparation stricte : `/guichet/**` CAISSIER uniquement, `/ventes-libres/**` MANAGER/ADMIN

### Ressources humaines & Paie
- Registre des employés avec poste et salaire de base
- Génération des bulletins de paie **mensuels ou hebdomadaires** (primes, indemnités, avance sur salaire)
- Paiement hebdomadaire : salaire de base ÷ 4, sélecteur de semaine dans le formulaire
- Calcul automatique CNPS employé (4,2 %) et patronal (16,2 %)
- Paiement des salaires depuis un compte bancaire avec vérification du solde
- Bulletin PDF téléchargeable par l'employé depuis son espace personnel

### Administration
- Gestion des utilisateurs et des rôles (SUPERADMIN / ADMIN / MANAGER / BOULANGER / MAGASINIER / CAISSIER)
- Paramètres : logo personnalisé, seuil d'incident production, largeur ticket thermique
- Rapports : stocks, production, livraisons, incidents, bulletins
- **Système de licence** : démo 90 jours, activation à vie par clé liée à l'ID machine
- Page **À propos** (`/a-propos`) avec les coordonnées du développeur

---

## Système de licence

Gestiboul intègre un système de licence hors-ligne :

| État | Description |
|------|-------------|
| **DEMO_ACTIVE** | Démo 90 jours à compter de la première installation — accès complet |
| **DEMO_EXPIRED** | Démo expirée — mode lecture seule (GET uniquement, toute écriture bloquée) |
| **FULL** | Licence complète activée à vie — accès complet permanent |

### Générer une clé pour un client

```bash
cd tools
javac KeyGenerator.java
java KeyGenerator NOM_CLIENT
# → GESTIBOUL-NOMCLIENT-XXXXXX
```

La clé est vérifiée hors-ligne (SHA-256 du salt + identifiant client). L'admin saisit la clé dans `/admin/licence`.

---

## Démarrage rapide

### Prérequis

- Java 17+
- MySQL 8 sur `localhost:3306`
- Maven (ou `./mvnw` inclus)

### Lancer en développement

```bash
# Mot de passe MySQL vide (par défaut)
./mvnw spring-boot:run

# Avec identifiants MySQL personnalisés
DB_USERNAME=root DB_PASSWORD=monmotdepasse ./mvnw spring-boot:run
```

L'application démarre sur **http://localhost:9000**

La base `boulangerie_bd` est créée automatiquement. Les données de démonstration (utilisateurs, produits, fournisseurs, stock…) sont injectées au premier démarrage.

### Build JAR exécutable

```bash
./mvnw package -DskipTests
java -jar target/gestion-boulangerie-0.0.1-SNAPSHOT.jar
```

### Déploiement production

```bash
java -jar gestion-boulangerie-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --DB_URL=jdbc:mysql://localhost:3306/boulangerie_bd \
  --DB_USERNAME=prod_user \
  --DB_PASSWORD=motdepasse_securise \
  --UPLOAD_DIR=/var/gestiboul/uploads
```

---

## Comptes utilisateurs (données de démonstration)

| Profil | Nom d'utilisateur | Mot de passe | Rôle |
|--------|------------------|--------------|------|
| Développeur | `hanga` | `Hanga@Gestiboul2024` | SUPERADMIN |
| Administrateur | `xavier` | `admin123` | ADMIN |
| Responsable | `manager` | `manager123` | MANAGER |
| Boulanger | `boulanger1` | `pain2024` | BOULANGER |
| Boulanger | `boulanger2` | `pain2024` | BOULANGER |
| Magasinier | `magasinier` | `stock2024` | MAGASINIER |
| Caissier | `caissier1` | `caisse2024` | CAISSIER |
| Caissier | `caissier2` | `caisse2024` | CAISSIER |

> Ces comptes sont créés automatiquement s'ils n'existent pas encore. Ne jamais utiliser ces mots de passe en production.

---

## Rôles et accès

### ADMIN
Accès total à toutes les fonctionnalités, y compris la gestion des utilisateurs, les paramètres système et la page de licence.

### MANAGER
Gestion opérationnelle quotidienne (commandes, production, livraisons, ventes, finances, paie, fournisseurs). Pas d'accès à l'administration système.

### BOULANGER
- Consulter les commandes en attente
- Lancer et valider la production du jour
- Signaler un incident de production
- Consulter et télécharger ses propres bulletins de paie

### MAGASINIER
- Consulter et gérer les niveaux de stock des matières premières
- Enregistrer les mouvements (ENTRÉE, SORTIE, RETOUR, PERTE)
- Consulter et télécharger ses propres bulletins de paie

---

## Flux métier principaux

### Cycle journalier

```
Manager crée les commandes par point de vente  →  /commandes
    ↓
Boulanger lance la production du jour  →  /production/passer-a-la-production
   (calcul des matières nécessaires + débit automatique du stock)
    ↓
Boulanger saisit les quantités réellement produites  →  /production/valider-production
   (réconciliation stock, détection d'incidents si écart > seuil)
    ↓
Manager crée la livraison  →  /livraisons/new
   (facture PDF générée automatiquement)
    ↓
Manager enregistre le revenu  →  Transaction VENTE sur le compte principal
```

### Charges fixes

```
Création de la charge (type, montant, périodicité, échéance)
    ↓
Paiement → choix du compte, vérification solde, Transaction CHARGE
    ↓
Si charge périodique : prochaine échéance créée automatiquement
    ↓
Reçu PDF téléchargeable  →  /comptabilite/charges-fixes/{id}/recu
```

### Paie mensuelle

```
Génération du bulletin  →  /bulletins/generer
   (salaire base + primes + indemnités - CNPS 4,2% - avance)
    ↓
Paiement  →  vérification solde, Transaction SALAIRE
    ↓
Bulletin PDF  →  /bulletins/mes-bulletins  (employé)  ou  /bulletins/{id}  (admin/manager)
```

---

## Fichiers générés

| Type | Mode | Stockage |
|------|------|----------|
| PDFs (bulletins, factures, reçus) | Streamé à la demande | Aucun — généré à la volée |
| Fichiers uploadés (logos…) | Persistant | `uploads/` (configurable via `UPLOAD_DIR`) |

---

## Variables d'environnement

| Variable | Défaut | Description |
|----------|--------|-------------|
| `DB_USERNAME` | `root` | Utilisateur MySQL |
| `DB_PASSWORD` | *(vide)* | Mot de passe MySQL |
| `DB_URL` | *(dev local)* | URL JDBC complète (prod) |
| `UPLOAD_DIR` | `uploads` | Répertoire des fichiers uploadés |
| `MAIL_USERNAME` | `changeme@gmail.com` | Compte Gmail pour les emails |
| `MAIL_PASSWORD` | `changeme` | Mot de passe d'application Gmail |

---

## Structure du projet

```
src/main/java/.../
├── config/         DataInitializer, WebMvcConfig, LicenseInterceptor, AppSettings
├── controller/     Contrôleurs web (un par module)
├── dto/            Objets de transfert (formulaires, vues)
├── exception/      Gestion globale des erreurs (GlobalExceptionHandler)
├── mapper/         Conversions Entity ↔ DTO
├── model/          Entités JPA
├── repository/     Interfaces Spring Data JPA
├── security/       SecurityConfig, UserDetailsService, rate limiting
└── service/        Logique métier

src/main/resources/
├── static/
│   ├── css/        Bootstrap 5 + styles personnalisés
│   ├── js/         scripts.js
│   └── fonts/      arial.ttf (pour les PDFs Flying Saucer)
└── templates/      Templates Thymeleaf (organisés par module)

tools/
└── KeyGenerator.java   Générateur de clés de licence (outil standalone)
```

---

## Tests

```bash
./mvnw test
```

81 tests unitaires couvrant les services principaux.

---

## Option bureau (Windows)

Pour distribuer Gestiboul comme une application de bureau sur un poste Windows :

```bat
REM start.bat
start "" "http://localhost:9000"
java -jar gestion-boulangerie.jar --spring.profiles.active=prod ...
```

L'utilisateur ouvre simplement `start.bat`. Le navigateur s'ouvre sur l'interface. Pour une installation entièrement autonome (sans MySQL séparé), remplacer MySQL par H2 en mode fichier et utiliser `jpackage` pour créer un `.exe` avec JRE bundlé.
