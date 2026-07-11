# Gestiboul — Scénario de présentation client

Ce document sert de guide pour faire une démo live de Gestiboul à un client (boulanger, investisseur, partenaire). Il liste les comptes de démonstration, un déroulé suggéré, et ce que chaque profil peut/ne peut pas faire.

---

## 1. Comptes de démonstration

| Profil | Utilisateur | Mot de passe | Rôle |
|---|---|---|---|
| Développeur | `hanga` | `Hanga@Gestiboul2024` | SUPERADMIN |
| Administrateur | `xavier` | `admin123` | ADMIN |
| Responsable | `manager` | `manager123` | MANAGER |
| Boulanger | `boulanger1` | `pain2024` | BOULANGER |
| Boulanger | `boulanger2` | `pain2024` | BOULANGER |
| Magasinier | `magasinier` | `stock2024` | MAGASINIER |
| Caissier | `caissier1` | `caisse2024` | CAISSIER |
| Caissier | `caissier2` | `caisse2024` | CAISSIER |

> ⚠️ Ne jamais utiliser ces mots de passe en production réelle chez un client — à changer dès l'installation définitive.

---

## 2. Déroulé de démo suggéré (~15-20 min)

L'idée est de suivre le cycle métier complet, dans l'ordre où un vrai utilisateur le vivrait, en changeant de compte à chaque étape pour montrer la séparation des rôles.

### Étape 1 — Vue d'ensemble (compte `xavier`, ADMIN)
1. Se connecter → tableau de bord : chiffres du jour, alertes de stock bas, accès rapides.
2. Montrer **Produits** (`/produits`) : catalogue avec recettes (matières premières nécessaires par produit) — c'est le cœur du calcul automatique.
3. Montrer **Paramètres** (`/admin/settings`) : logo personnalisé, seuil d'incident production, largeur ticket — personnalisation rapide pour le client.

### Étape 2 — Commande (compte `manager` ou `xavier`)
4. Créer une commande (`/commandes/new`) pour un point de vente, avec 2-3 produits.
5. Montrer la liste des commandes en attente.

### Étape 3 — Production (compte `boulanger1`, BOULANGER)
6. Se déconnecter, se reconnecter en boulanger.
7. **Démarrer la production** (`/production/passer-a-la-production`) : montrer le calcul automatique des matières premières nécessaires à partir des commandes du jour — c'est l'argument de vente principal (fini le calcul à la main).
8. **Valider la production** : saisir les quantités réellement produites → montrer la comparaison théorique/réel et la détection d'écart.

### Étape 4 — Livraison (compte `manager`)
9. Créer une livraison depuis la production du jour (`/livraisons/new`) — chargement dynamique des produits disponibles.
10. Montrer la **facture PDF générée automatiquement**.
11. Enregistrer le revenu → montrer l'impact immédiat sur le solde du compte bancaire.

### Étape 5 — Guichet caisse (compte `caissier1`, CAISSIER)
12. Se reconnecter en caissier → interface tactile avec tuiles produits.
13. Faire une vente rapide (+/− par produit), encaisser (Espèces ou Mobile Money), imprimer le ticket.
14. C'est l'écran qui parle le plus visuellement à un client — à ne pas sauter.

### Étape 6 — Stock (compte `magasinier`, MAGASINIER)
15. Montrer une réception fournisseur (`/matieres-premieres/add-stock`) : saisie quantité commandée + avaries → quantité nette calculée automatiquement, achat enregistré en comptabilité.
16. Montrer l'historique des mouvements de stock (traçabilité complète).

### Étape 7 — Finances (retour `xavier`, ADMIN)
17. Tableau de bord financier (`/comptabilite/dashboard`) : soldes, flux, KPIs.
18. Paie : génération d'un bulletin de salaire avec calcul CNPS automatique.

### Clôture
19. Rappeler que tout fonctionne **sans connexion internet** (hébergement local, pas de dépendance cloud) — argument fort pour un client qui n'a pas toujours une connexion fiable.

---

## 3. Accès par rôle (résumé)

| Rôle | Peut faire | Ne peut pas faire |
|---|---|---|
| **SUPERADMIN / ADMIN** | Tout : commandes, production, livraisons, ventes, finances, paie, fournisseurs, gestion utilisateurs, paramètres système, licence | — |
| **MANAGER** | Commandes, production, livraisons, ventes, finances, paie, fournisseurs (gestion opérationnelle complète) | Administration système (utilisateurs, paramètres, licence) |
| **BOULANGER** | Consulter commandes en attente, lancer/valider la production, signaler un incident, ses propres bulletins de paie | Livraisons, finances, gestion des stocks, administration |
| **MAGASINIER** | Consulter/gérer les stocks de matières premières (ENTRÉE/SORTIE/RETOUR/PERTE), ses propres bulletins de paie | Commandes, production, livraisons, finances, administration |
| **CAISSIER** | Uniquement l'interface guichet (`/guichet/**`) : vente directe, encaissement, ticket | Absolument tout le reste — accès volontairement restreint à la caisse |

Cette séparation stricte des rôles est elle-même un argument de vente : chaque employé ne voit que ce dont il a besoin, ce qui réduit les erreurs et protège les données sensibles (finances, paie).

---

## 4. Points forts à mettre en avant

- **Calcul automatique des matières premières** à partir des commandes — élimine les erreurs de calcul manuel du boulanger.
- **Détection automatique des incidents de production** (écart théorique/réel au-delà d'un seuil configurable).
- **Facturation et reçus PDF automatiques** — livraisons, charges fixes, bulletins de paie.
- **Fonctionne 100% hors-ligne** — aucune dépendance à une connexion internet une fois installé (y compris les icônes et polices, corrigé lors de cette session).
- **Système de licence** : démo 90 jours gratuite, puis activation à vie par clé — pas d'abonnement récurrent.
- **Traçabilité complète** : chaque mouvement de stock, chaque transaction est horodaté et attribué à un utilisateur.

## 5. Conseils pratiques pour la démo

- Préparer les données de démo **avant** le rendez-vous (produits, quelques commandes déjà traitées) pour ne pas partir d'un tableau de bord vide — mais garder au moins une commande "En attente" pour montrer le cycle en direct.
- Tester le scénario complet une fois avant la démo réelle — certains écrans (upload logo, livraison) ont des contraintes de validation (ex. taille de fichier max 2 Mo) qu'il vaut mieux connaître à l'avance.
- Avoir `deploy\start-gestiboul.bat` déjà lancé et l'appli chargée en arrière-plan avant l'arrivée du client, pour éviter un temps d'attente au démarrage pendant la démo.
