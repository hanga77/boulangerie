# Guichet POS Caissier — Design Document

**Goal:** Ajouter un rôle CAISSIER avec une interface POS tactile (tuiles +/−) pour enregistrer les ventes au guichet avec choix du moyen de paiement (Espèces / Mobile Money).

**Architecture:** Extension de VenteLibre existant — ajout du rôle CAISSIER, d'un champ `moyenPaiement` sur VenteLibre, et d'un nouveau contrôleur `/guichet` avec interface POS dédiée. La logique métier (débit stock, transaction financière) est entièrement réutilisée depuis `VenteLibreService`.

**Tech Stack:** Spring Boot 3.4.2 · Thymeleaf · Spring Security (hiérarchie de rôles existante) · MySQL 8

---

## 1. Rôle et accès

### Nouveau rôle : `CAISSIER`
- Positionné au même niveau que BOULANGER et MAGASINIER dans la hiérarchie (pas de droits admin hérités)
- Comptes de démonstration : `caissier1 / caisse2024`, `caissier2 / caisse2024`

### Accès CAISSIER
| URL | Permission |
|-----|-----------|
| `/guichet/**` | CAISSIER, ADMIN, MANAGER |
| `/bulletins/mes-bulletins` | déjà accessible (authenticated) |
| `/ventes-libres/**` | ADMIN, MANAGER uniquement (inchangé) |
| Tout le reste | Accès refusé |

### Navbar CAISSIER
Le caissier voit uniquement :
- Accueil (dashboard)
- Bouton "Ouvrir le guichet" → `/guichet`
- Mes bulletins
- Déconnexion

---

## 2. Modèle de données

### `MoyenPaiement` (nouveau enum)
```java
public enum MoyenPaiement {
    ESPECES, MOBILE_MONEY
}
```

### `VenteLibre` (modification)
Ajout d'un champ nullable :
```java
@Enumerated(EnumType.STRING)
private MoyenPaiement moyenPaiement; // null pour ventes manager existantes → affiché "Espèces"
```

### `Transaction` — description enrichie
Format pour ventes guichet : `"Vente guichet [Nom Guichet] — Espèces"` ou `"... — Mobile Money"`

---

## 3. Contrôleur `/guichet`

### `GuichetController`

**GET `/guichet`** — Sélection du guichet
- Charge tous les guichets actifs (`guichetRepository.findByActifTrue()`)
- Renvoie `guichet/select.html`
- Accessible : CAISSIER, ADMIN, MANAGER

**GET `/guichet/vente?guichetId={id}`** — Écran POS
- Charge le guichet
- Trouve la production du jour (`LocalDate.now()`) ou de la veille si aucune aujourd'hui
- Charge les `produitsRestants` de la production trouvée
- Renvoie `guichet/pos.html` avec : guichet, production, produitsRestants (Map<Produit, Integer>), moyensPaiement

**POST `/guichet/vente`** — Enregistrement de la vente
- Paramètres : `guichetId`, `productionId`, `moyenPaiement`, `produits[{id}]` (quantités)
- Filtre les produits à quantité > 0
- Si aucun produit sélectionné → retour POS avec erreur
- Appelle `venteLibreService.createVenteLibre(productionId, guichetId, produitsVendus, currentUser, moyenPaiement)`
- Succès → redirect `/guichet/vente?guichetId={id}` (prêt client suivant)
- Erreur stock → redirect avec message d'erreur

---

## 4. Service `VenteLibreService`

### Modification de `createVenteLibre()`
Signature actuelle :
```java
createVenteLibre(Long productionId, Long guichetId, Map<Long, Integer> produits, User user)
```
Nouvelle signature :
```java
createVenteLibre(Long productionId, Long guichetId, Map<Long, Integer> produits, User user, MoyenPaiement moyenPaiement)
```
- `moyenPaiement` peut être `null` (appels manager existants) → traité comme ESPECES
- Description transaction enrichie si `moyenPaiement != null` et guichet non null

---

## 5. Templates

### `guichet/select.html`
- Navbar simplifiée (ou navbar standard avec droits CAISSIER)
- Titre : "Choisir votre guichet"
- Dropdown ou liste de boutons : un par guichet actif (nom + point de vente)
- Bouton "Démarrer la vente"

### `guichet/pos.html`
Interface POS plein écran :

**En-tête** : Nom guichet · Date · Utilisateur connecté · Lien "Changer de guichet"

**Grille de tuiles** (une par produit disponible) :
- Tuile bleue (`bg-primary`) si stock > 5
- Tuile rouge (`bg-danger`) si stock ≤ 5
- Tuile grise désactivée si stock = 0
- Chaque tuile : icône pain, nom produit, prix unitaire, stock restant, boutons −/+ avec compteur JS
- Compteur JS ne peut pas descendre sous 0 ni dépasser le stock restant

**Barre du bas** :
- Sélecteur moyen de paiement : `Espèces` (défaut, bouton bleu) / `Mobile Money` (bouton gris → bleu si sélectionné)
- Total calculé en JS (mise à jour en temps réel)
- Récapitulatif ligne : "X × Produit"
- Bouton `✓ ENCAISSER` (vert, désactivé si total = 0)

**Après encaissement réussi** : flash message de confirmation → POS se recharge, tous les compteurs à 0

---

## 6. Flux complet

```
Caissier se connecte → Dashboard
    ↓
"Ouvrir le guichet" → GET /guichet
    ↓
Sélection guichet → GET /guichet/vente?guichetId=X
    ↓ (production du jour ou veille chargée automatiquement)
Écran POS : tuiles produits disponibles
    ↓
Caissier ajuste quantités +/− pour chaque produit vendu
    ↓
Sélectionne moyen de paiement (Espèces par défaut)
    ↓
Clique ENCAISSER → POST /guichet/vente
    ↓
VenteLibreService.createVenteLibre() :
  · Valide stock disponible pour chaque produit
  · Déduit produitsRestants sur Production
  · Crée VenteLibre (guichet, user, moyenPaiement, montantTotal, produitsVendus)
  · Crée Transaction VENTE_LIBRE sur Compte Principal
    ↓
Retour POS → prêt pour client suivant
```

---

## 7. Gestion des erreurs

| Situation | Comportement |
|-----------|-------------|
| Aucune production du jour ni de la veille | Message d'alerte : "Aucune production disponible aujourd'hui. Contactez le manager." |
| Stock insuffisant au moment de valider | Message d'erreur, retour POS sans encaissement |
| Guichet inactif | Retiré de la liste de sélection |
| Aucun produit sélectionné (total = 0) | Bouton ENCAISSER désactivé côté JS + vérification serveur |

---

## 8. Impression thermique (ticket de caisse)

### Principe technique
Impression via `window.print()` avec CSS `@media print`. Le navigateur envoie directement à l'imprimante thermique connectée en USB ou réseau. Aucun driver spécifique requis côté serveur.

### Déclenchement
Après un encaissement réussi, la page POS affiche un bouton **"🖨 Imprimer le ticket"**. Le caissier clique s'il veut imprimer — pas d'impression automatique.

### Format du ticket
Largeur configurable dans Paramètres admin (`AppSettings.largeurTicketMm`) : `58` ou `80` (valeur par défaut : `80`).

CSS appliqué dynamiquement :
```css
@media print {
  @page { size: {largeurTicketMm}mm auto; margin: 3mm; }
  body * { visibility: hidden; }
  #ticket-caisse, #ticket-caisse * { visibility: visible; }
  #ticket-caisse { position: absolute; top: 0; left: 0; width: 100%; font-size: 11px; font-family: monospace; }
}
```

### Contenu du ticket (`#ticket-caisse`)
```
================================
       [Nom boulangerie]
       [Adresse si configurée]
================================
Guichet : [Nom guichet]
Date    : 30/05/2026  14:32
Caissier: caissier1
--------------------------------
Baguette           x3   750 XAF
Croissant          x2   700 XAF
--------------------------------
TOTAL                 1 450 XAF
Paiement : Espèces
================================
   Merci pour votre achat !
   Gestiboul — Hanga J.F.
================================
```

### Implémentation
- Fragment Thymeleaf `templates/guichet/ticket-fragment.html` avec div `#ticket-caisse`
- Inclus dans `pos.html` (masqué à l'écran, visible uniquement à l'impression)
- Le CSS `@page size` est injecté dynamiquement via une variable Thymeleaf `${appSettings.largeurTicketMm}`
- `AppSettings` : ajouter `Integer largeurTicketMm = 80`
- Page `/admin/settings` : ajouter le champ de sélection 58mm / 80mm

---

## 9. Fichiers à créer / modifier

| Fichier | Action |
|---------|--------|
| `model/MoyenPaiement.java` | Créer |
| `model/VenteLibre.java` | Modifier — ajouter `moyenPaiement` |
| `model/AppSettings.java` | Modifier — ajouter `largeurTicketMm` |
| `controller/GuichetController.java` | Créer |
| `service/VenteLibreService.java` | Modifier — ajouter param `moyenPaiement` |
| `templates/guichet/select.html` | Créer |
| `templates/guichet/pos.html` | Créer (inclut ticket-fragment) |
| `templates/guichet/ticket-fragment.html` | Créer |
| `templates/admin/settings.html` | Modifier — ajouter largeur ticket |
| `templates/fragments/layout.html` | Modifier — navbar CAISSIER |
| `config/DataInitializer.java` | Modifier — ajouter caissier1, caissier2 |
| `security/SecurityConfig.java` | Modifier — `/guichet/**` → CAISSIER + ADMIN + MANAGER |

---

## 10. Hors scope

- Clôture de caisse journalière (pas demandé)
- Annulation de vente par le caissier (MANAGER uniquement, via interface existante)
- Paiement carte bancaire
- Guichet fixe par utilisateur (caissier choisit à chaque session)
- Driver d'imprimante côté serveur (impression via navigateur uniquement)
