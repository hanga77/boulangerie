# Design — Reçus de paiement des charges fixes

**Date :** 2026-05-26
**Projet :** Gestion Boulangerie (Spring Boot 3.4.2 / Thymeleaf / MySQL)
**Périmètre :** Sous-projet C — Reçu PDF pour le paiement des charges fixes

---

## 1. Contexte et objectif

Actuellement, payer une charge fixe (`GET /comptabilite/charges-fixes/{id}/payer`) marque simplement `paye=true` et crée la prochaine échéance. Aucune Transaction n'est créée, aucun compte bancaire n'est débité, aucun document n'est généré.

**Objectifs :**
- Intégrer le paiement d'une charge dans le système financier : créer une `Transaction` et débiter le `CompteBancaire` choisi par l'utilisateur
- Générer un reçu PDF téléchargeable après chaque paiement
- Ne pas créer de nouvelle entité — le reçu est généré à la volée depuis la `ChargeFixe` et sa `Transaction` liée

---

## 2. Modèle de données

### 2.1 Modifications de l'entité `ChargeFixe`

Deux nouveaux champs ajoutés (colonnes nullable, pas de migration destructive) :

```java
private LocalDate datePaiement;       // null tant que paye=false

@OneToOne(optional = true)
private Transaction transaction;      // lié au moment du paiement
```

### 2.2 Règle de paiement

```
Transaction.type           = "CHARGE"
Transaction.montant        = charge.getMontant()
Transaction.date           = LocalDate.now()
Transaction.description    = "Paiement [charge.getType()] — [charge.getDescription()]"
Transaction.compteBancaire = compte choisi par l'utilisateur

compte.setSolde(compte.getSolde() - charge.getMontant())

charge.setPaye(true)
charge.setDatePaiement(LocalDate.now())
charge.setTransaction(savedTransaction)
```

La méthode `creerProchaineEcheance()` existante est appelée après, sans modification.

---

## 3. Composants backend

| Composant | Type | Modification |
|---|---|---|
| `ChargeFixe` | Entity | +`datePaiement`, +`transaction` |
| `ChargeFixeService.payerChargeFixe()` | Service | +paramètre `Long compteBancaireId`, crée Transaction, débite compte |
| `CompteBancaireRepository` | Repository | Utilisé (déjà existant) |
| `TransactionRepository` | Repository | Utilisé (déjà existant) |
| `ComptabiliteController` | Controller | GET `/{id}/payer` → formulaire ; POST `/{id}/payer` → traitement |
| `PdfController` | Controller | +`GET /comptabilite/charges-fixes/{id}/recu` |

---

## 4. Logique métier

### 4.1 `payerChargeFixe(Long id, Long compteBancaireId)`

```
1. Charger la ChargeFixe → ResourceNotFoundException si absente
2. Vérifier charge.paye == false → IllegalStateException si déjà payée
3. Charger le CompteBancaire par id → ResourceNotFoundException si absent
4. Créer Transaction(type="CHARGE", montant, date=today, description, compteBancaire)
5. Débiter compte.solde -= charge.montant
6. Sauvegarder compte + transaction
7. Mettre à jour charge : paye=true, datePaiement=today, transaction=savedTx
8. Appeler creerProchaineEcheance(charge) — inchangé
9. Sauvegarder charge
```

### 4.2 Route PDF `GET /comptabilite/charges-fixes/{id}/recu`

```
1. Charger la ChargeFixe
2. Vérifier charge.paye == true → erreur si non payée (reçu indisponible)
3. Construire le contexte Thymeleaf (charge + appName + logo)
4. Traiter le template "comptabilite/recu-charge-template"
5. Réponse : Content-Type=application/pdf
              filename=recu-charge-{id}-{datePaiement}.pdf
6. Rendu via ITextRenderer + arial.ttf (pattern existant)
```

---

## 5. Pages

### 5.1 `GET /comptabilite/charges-fixes/{id}/payer`

- Accès : ADMIN, MANAGER
- Affiche : résumé de la charge (type, description, montant, échéance)
- Formulaire : `<select>` des comptes bancaires avec leur solde actuel
- Alerte JS si solde insuffisant (vérification visuelle côté client)
- Bouton "Confirmer le paiement" + lien "Annuler"

### 5.2 `POST /comptabilite/charges-fixes/{id}/payer`

- Accès : ADMIN, MANAGER
- Paramètre : `compteBancaireId`
- Appelle `payerChargeFixe(id, compteBancaireId)`
- Gère `IllegalStateException` (charge déjà payée) → flash errorMessage
- Redirige vers `/comptabilite/charges-fixes` avec successMessage

### 5.3 `GET /comptabilite/charges-fixes/{id}/recu`

- Accès : ADMIN, MANAGER
- Génère et télécharge le PDF via Flying Saucer

### 5.4 Modification `comptabilite/charges-fixes.html`

- Le bouton "Payer" devient un lien `href` vers `GET /{id}/payer` (au lieu du GET direct qui payait immédiatement)
- Sur les lignes `paye=true` : ajout d'un bouton **Reçu PDF** (`fas fa-file-pdf`) lié à `/{id}/recu`

---

## 6. Template PDF

Fichier : `src/main/resources/templates/comptabilite/recu-charge-template.html`

Structure du reçu :
- En-tête : nom boulangerie (appName), logo si disponible, "REÇU DE PAIEMENT"
- Informations de la charge :
  | Libellé | Valeur |
  |---|---|
  | Type de charge | LOYER / ELECTRICITE / ... |
  | Description | texte libre |
  | Montant | X XAF |
  | Date d'échéance | dd/MM/yyyy |
  | Date de paiement | dd/MM/yyyy |
  | Compte débité | nom du compte |
  | Référence transaction | `#transaction.id` |
- Pied de page : signature employeur, date
- Inline CSS uniquement (Flying Saucer, pas de `border-radius`)

---

## 7. Sécurité

| Route | Rôle requis |
|---|---|
| `GET /comptabilite/charges-fixes/{id}/payer` | ADMIN, MANAGER |
| `POST /comptabilite/charges-fixes/{id}/payer` | ADMIN, MANAGER |
| `GET /comptabilite/charges-fixes/{id}/recu` | ADMIN, MANAGER |

Le pattern URL `/comptabilite/**` est déjà couvert par les règles Spring Security existantes (anyRequest().authenticated() + @PreAuthorize method-level).

---

## 8. Ce qui n'est PAS dans ce périmètre

- Gestion de factures fournisseurs reçues (bons de commande, livraisons)
- Import/export CSV des charges
- Workflow de validation multi-niveaux
- Historique des modifications de charge après paiement
- Notification email du reçu
