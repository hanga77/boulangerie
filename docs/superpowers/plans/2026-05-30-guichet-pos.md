# Guichet POS Caissier — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ajouter un rôle CAISSIER avec interface POS (tuiles +/−, Espèces/Mobile Money) pour enregistrer des ventes au guichet avec impression ticket thermique.

**Architecture:** Extension de VenteLibre existant — ajout de `MoyenPaiement` enum + champ sur `VenteLibre`, nouveau `GuichetController` à `/guichet`, réutilisation de `VenteLibreService.createVenteLibre()` via `CreateVenteLibreRequest` enrichi. Impression ticket via `window.print()` CSS @media print.

**Tech Stack:** Spring Boot 3.4.2 · Java 17 · Thymeleaf · Spring Security · MySQL 8 · JUnit 5 + Mockito

---

## Structure des fichiers

| Fichier | Action |
|---------|--------|
| `src/main/java/lab/hang/Gestion/boulangerie/model/MoyenPaiement.java` | Créer |
| `src/main/java/lab/hang/Gestion/boulangerie/model/VenteLibre.java` | Modifier — ajouter champ `moyenPaiement` |
| `src/main/java/lab/hang/Gestion/boulangerie/dto/CreateVenteLibreRequest.java` | Modifier — ajouter champ `moyenPaiement` |
| `src/main/java/lab/hang/Gestion/boulangerie/service/VenteLibreService.java` | Modifier — description transaction enrichie |
| `src/main/java/lab/hang/Gestion/boulangerie/service/AppSettingsService.java` | Modifier — ajouter `getLargeurTicketMm()` / `updateLargeurTicketMm()` |
| `src/main/java/lab/hang/Gestion/boulangerie/controller/GuichetController.java` | Créer |
| `src/main/java/lab/hang/Gestion/boulangerie/security/SecurityConfig.java` | Modifier — ajouter `/guichet/**` |
| `src/main/java/lab/hang/Gestion/boulangerie/config/DataInitializer.java` | Modifier — caissier1/caissier2 + ticket.largeur |
| `src/main/resources/templates/guichet/select.html` | Créer |
| `src/main/resources/templates/guichet/pos.html` | Créer |
| `src/main/resources/templates/guichet/ticket-fragment.html` | Créer |
| `src/main/resources/templates/admin/settings.html` | Modifier — champ largeur ticket |
| `src/main/resources/templates/fragments/layout.html` | Modifier — lien guichet pour CAISSIER |
| `src/test/java/lab/hang/gestion_boulangerie/service/VenteLibreServiceTest.java` | Créer |

---

### Task 1 : Enum MoyenPaiement + modèle VenteLibre + DTO

**Files:**
- Create: `src/main/java/lab/hang/Gestion/boulangerie/model/MoyenPaiement.java`
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/model/VenteLibre.java`
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/dto/CreateVenteLibreRequest.java`

- [ ] **Step 1 : Créer l'enum MoyenPaiement**

```java
// src/main/java/lab/hang/Gestion/boulangerie/model/MoyenPaiement.java
package lab.hang.Gestion.boulangerie.model;

public enum MoyenPaiement {
    ESPECES, MOBILE_MONEY
}
```

- [ ] **Step 2 : Ajouter le champ `moyenPaiement` sur VenteLibre**

Dans `VenteLibre.java`, après le champ `montantTotal`, ajouter :

```java
@Enumerated(EnumType.STRING)
private MoyenPaiement moyenPaiement; // null = vente manager (traité comme ESPECES)
```

Importer : `import lab.hang.Gestion.boulangerie.model.MoyenPaiement;`

- [ ] **Step 3 : Ajouter `moyenPaiement` dans CreateVenteLibreRequest**

Dans `CreateVenteLibreRequest.java`, ajouter :

```java
private MoyenPaiement moyenPaiement;

public MoyenPaiement getMoyenPaiement() { return moyenPaiement; }
public void setMoyenPaiement(MoyenPaiement moyenPaiement) { this.moyenPaiement = moyenPaiement; }
```

Importer : `import lab.hang.Gestion.boulangerie.model.MoyenPaiement;`

- [ ] **Step 4 : Compiler**

```bash
./mvnw compile -q
```

Expected : BUILD SUCCESS

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/model/MoyenPaiement.java \
        src/main/java/lab/hang/Gestion/boulangerie/model/VenteLibre.java \
        src/main/java/lab/hang/Gestion/boulangerie/dto/CreateVenteLibreRequest.java
git commit -m "feat(pos): MoyenPaiement enum + champ VenteLibre + DTO"
```

---

### Task 2 : VenteLibreService — description transaction enrichie + test

**Files:**
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/service/VenteLibreService.java`
- Create: `src/test/java/lab/hang/gestion_boulangerie/service/VenteLibreServiceTest.java`

- [ ] **Step 1 : Écrire le test**

```java
// src/test/java/lab/hang/gestion_boulangerie/service/VenteLibreServiceTest.java
package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.CreateVenteLibreRequest;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.*;
import lab.hang.Gestion.boulangerie.service.VenteLibreService;
import lab.hang.Gestion.boulangerie.service.UserService;
import lab.hang.Gestion.boulangerie.service.PointDeVenteService;
import lab.hang.Gestion.boulangerie.mapper.VenteLibreMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenteLibreServiceTest {

    @Mock private VenteLibreRepository venteLibreRepository;
    @Mock private ProductionRepository productionRepository;
    @Mock private ProduitRepository produitRepository;
    @Mock private CompteBancaireRepository compteBancaireRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserService userService;
    @Mock private PointDeVenteService pointDeVenteService;
    @Mock private VenteLibreMapper venteLibreMapper;
    @InjectMocks
    private VenteLibreService venteLibreService;

    @Test
    void createVenteLibre_guichetMobileMoney_descriptionEnrichie() {
        // Arrange
        Produit baguette = new Produit();
        baguette.setId(1L);
        baguette.setNom("Baguette");
        baguette.setPrix(250.0);

        Map<Produit, Integer> produitsRestants = new HashMap<>();
        produitsRestants.put(baguette, 10);

        Production production = new Production();
        production.setId(1L);
        production.setDateProduction(LocalDate.now());
        production.setProduitsRestants(produitsRestants);

        Guichet guichet = new Guichet();
        guichet.setId(1L);
        guichet.setNom("Guichet 1");

        CompteBancaire compte = new CompteBancaire();
        compte.setNom("Compte Principal");
        compte.setSolde(0.0);

        User user = new User();
        user.setUsername("caissier1");

        when(userService.getCurrentUser()).thenReturn(user);
        when(productionRepository.findById(1L)).thenReturn(Optional.of(production));
        when(pointDeVenteService.getGuichetEntityById(1L)).thenReturn(guichet);
        when(produitRepository.findById(1L)).thenReturn(Optional.of(baguette));
        when(venteLibreRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(Optional.of(compte));
        when(venteLibreMapper.toDTO(any())).thenReturn(null);

        CreateVenteLibreRequest request = new CreateVenteLibreRequest();
        request.setProductionId(1L);
        request.setGuichetId(1L);
        request.setMoyenPaiement(MoyenPaiement.MOBILE_MONEY);
        request.setProduits(Map.of(1L, 3));

        // Act
        venteLibreService.createVenteLibre(request);

        // Assert — la description de la transaction contient "Mobile Money" et "Guichet 1"
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        String description = txCaptor.getValue().getDescription();
        assertThat(description).contains("Guichet 1");
        assertThat(description).contains("Mobile Money");
    }

    @Test
    void createVenteLibre_sansGuichet_descriptionGenerique() {
        Produit baguette = new Produit();
        baguette.setId(1L);
        baguette.setNom("Baguette");
        baguette.setPrix(250.0);

        Map<Produit, Integer> produitsRestants = new HashMap<>();
        produitsRestants.put(baguette, 10);

        Production production = new Production();
        production.setId(1L);
        production.setDateProduction(LocalDate.now());
        production.setProduitsRestants(produitsRestants);

        CompteBancaire compte = new CompteBancaire();
        compte.setNom("Compte Principal");
        compte.setSolde(0.0);

        User user = new User();
        user.setUsername("manager");

        when(userService.getCurrentUser()).thenReturn(user);
        when(productionRepository.findById(1L)).thenReturn(Optional.of(production));
        when(produitRepository.findById(1L)).thenReturn(Optional.of(baguette));
        when(venteLibreRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(Optional.of(compte));
        when(venteLibreMapper.toDTO(any())).thenReturn(null);

        CreateVenteLibreRequest request = new CreateVenteLibreRequest();
        request.setProductionId(1L);
        request.setProduits(Map.of(1L, 2));
        // pas de guichetId ni moyenPaiement

        venteLibreService.createVenteLibre(request);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getDescription()).startsWith("Vente libre ID:");
    }
}
```

- [ ] **Step 2 : Lancer le test pour vérifier qu'il échoue**

```bash
./mvnw test -pl . -Dtest=VenteLibreServiceTest -q 2>&1 | tail -20
```

Expected : FAIL — `description` ne contient pas "Guichet 1"

- [ ] **Step 3 : Modifier `enregistrerRevenu()` dans VenteLibreService**

Remplacer la méthode `enregistrerRevenu` et son appel dans `createVenteLibre` :

```java
// Dans createVenteLibre(), remplacer la dernière ligne :
// enregistrerRevenu(saved.getId(), montantTotal);
// Par :
enregistrerRevenu(saved, montantTotal);
```

Nouvelle méthode `enregistrerRevenu` :

```java
private void enregistrerRevenu(VenteLibre vente, double montantTotal) {
    CompteBancaire compte = compteBancaireRepository.findByNom("Compte Principal")
            .orElseThrow(() -> new ResourceNotFoundException("Compte bancaire principal non trouvé"));
    compte.setSolde(compte.getSolde() + montantTotal);

    String description;
    if (vente.getGuichet() != null) {
        String paiement = vente.getMoyenPaiement() == MoyenPaiement.MOBILE_MONEY
                ? "Mobile Money" : "Espèces";
        description = "Vente guichet [" + vente.getGuichet().getNom() + "] — " + paiement;
    } else {
        description = "Vente libre ID: " + vente.getId();
    }

    Transaction transaction = new Transaction();
    transaction.setDate(LocalDate.now());
    transaction.setType("VENTE_LIBRE");
    transaction.setMontant(montantTotal);
    transaction.setDescription(description);
    transaction.setCompteBancaire(compte);

    transactionRepository.save(transaction);
    compteBancaireRepository.save(compte);
}
```

Dans `createVenteLibre()`, après `venteLibre.setGuichet(...)`, ajouter :

```java
venteLibre.setMoyenPaiement(request.getMoyenPaiement());
```

- [ ] **Step 4 : Lancer le test**

```bash
./mvnw test -pl . -Dtest=VenteLibreServiceTest -q 2>&1 | tail -10
```

Expected : Tests run: 2, Failures: 0

- [ ] **Step 5 : Lancer tous les tests**

```bash
./mvnw test -q 2>&1 | tail -10
```

Expected : BUILD SUCCESS — tous les tests existants toujours verts

- [ ] **Step 6 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/service/VenteLibreService.java \
        src/test/java/lab/hang/gestion_boulangerie/service/VenteLibreServiceTest.java
git commit -m "feat(pos): description transaction enrichie guichet + moyenPaiement"
```

---

### Task 3 : Sécurité + comptes CAISSIER

**Files:**
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/security/SecurityConfig.java`
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/config/DataInitializer.java`

- [ ] **Step 1 : Ajouter `/guichet/**` dans SecurityConfig**

Dans le bloc `authorizeHttpRequests`, après la ligne `/ventes-libres/**`, ajouter :

```java
.requestMatchers(mvcMatcherBuilder.pattern("/guichet/**")).hasAnyRole("ADMIN", "MANAGER", "CAISSIER")
```

- [ ] **Step 2 : Ajouter caissier1 et caissier2 dans DataInitializer**

Dans la méthode `initUsers()`, ajouter après la ligne `ensureUser("magasinier", ...)` :

```java
ensureUser("caissier1", "caisse2024", "CAISSIER", true);
ensureUser("caissier2", "caisse2024", "CAISSIER", true);
```

- [ ] **Step 3 : Compiler**

```bash
./mvnw compile -q
```

Expected : BUILD SUCCESS

- [ ] **Step 4 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/security/SecurityConfig.java \
        src/main/java/lab/hang/Gestion/boulangerie/config/DataInitializer.java
git commit -m "feat(pos): rôle CAISSIER — sécurité /guichet/** + comptes démo"
```

---

### Task 4 : Paramètre largeur ticket dans AppSettings

**Files:**
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/service/AppSettingsService.java`
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/config/DataInitializer.java`
- Modify: `src/main/resources/templates/admin/settings.html`

- [ ] **Step 1 : Ajouter les méthodes dans AppSettingsService**

```java
public int getLargeurTicketMm() {
    return appSettingsRepository.findById("ticket.largeur")
            .map(s -> Integer.parseInt(s.getValeur()))
            .orElse(80);
}

@Transactional
public void updateLargeurTicketMm(int largeur) {
    AppSettings setting = appSettingsRepository.findById("ticket.largeur")
            .orElse(new AppSettings("ticket.largeur", "80"));
    setting.setValeur(String.valueOf(largeur));
    appSettingsRepository.save(setting);
}
```

Vérifier que `appSettingsRepository` est déjà injecté dans la classe (il l'est via les autres méthodes existantes).

- [ ] **Step 2 : Seed la valeur dans DataInitializer**

Trouver la méthode `initAppSettings()` (ou équivalent) dans `DataInitializer.java`. Si une méthode similaire initialise les settings, y ajouter :

```java
if (appSettingsRepository.findById("ticket.largeur").isEmpty()) {
    appSettingsRepository.save(new AppSettings("ticket.largeur", "80"));
}
```

Si aucune méthode d'init settings n'existe, créer la logique directement dans `run()` :

```java
if (appSettingsRepository.findById("ticket.largeur").isEmpty()) {
    appSettingsRepository.save(new AppSettings("ticket.largeur", "80"));
}
```

- [ ] **Step 3 : Ajouter le champ dans settings.html**

Trouver la fin du dernier `<div class="card">` dans settings.html, avant le `</main>`, et ajouter une nouvelle carte :

```html
<!-- Ticket de caisse -->
<div class="card mb-4">
    <div class="card-header">
        <i class="fas fa-receipt me-2"></i>Impression ticket de caisse
    </div>
    <div class="card-body">
        <form th:action="@{/admin/settings/ticket-largeur}" method="post">
            <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
            <div class="mb-3">
                <label class="form-label fw-semibold">Largeur du papier thermique</label>
                <div class="d-flex gap-3">
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="largeur" value="58"
                               id="r58" th:checked="${largeurTicketMm == 58}">
                        <label class="form-check-label" for="r58">58 mm</label>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="largeur" value="80"
                               id="r80" th:checked="${largeurTicketMm == 80}">
                        <label class="form-check-label" for="r80">80 mm (défaut)</label>
                    </div>
                </div>
                <div class="form-text">Correspond à la largeur du rouleau dans votre imprimante thermique.</div>
            </div>
            <button type="submit" class="btn btn-primary btn-sm">
                <i class="fas fa-save me-1"></i>Enregistrer
            </button>
        </form>
    </div>
</div>
```

- [ ] **Step 4 : Ajouter l'endpoint dans le contrôleur AdminSettingsController (ou équivalent)**

Trouver le contrôleur qui gère `/admin/settings`. Y ajouter :

```java
@PostMapping("/admin/settings/ticket-largeur")
@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
public String updateLargeurTicket(@RequestParam int largeur, RedirectAttributes ra) {
    if (largeur != 58 && largeur != 80) largeur = 80;
    appSettingsService.updateLargeurTicketMm(largeur);
    ra.addFlashAttribute("successMessage", "Largeur ticket mise à jour : " + largeur + " mm");
    return "redirect:/admin/settings";
}
```

Ajouter aussi `largeurTicketMm` au model de la page GET settings :

```java
model.addAttribute("largeurTicketMm", appSettingsService.getLargeurTicketMm());
```

- [ ] **Step 5 : Compiler**

```bash
./mvnw compile -q
```

Expected : BUILD SUCCESS

- [ ] **Step 6 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/service/AppSettingsService.java \
        src/main/java/lab/hang/Gestion/boulangerie/config/DataInitializer.java \
        src/main/resources/templates/admin/settings.html
git commit -m "feat(pos): paramètre largeur ticket thermique 58/80mm"
```

---

### Task 5 : GuichetController

**Files:**
- Create: `src/main/java/lab/hang/Gestion/boulangerie/controller/GuichetController.java`

- [ ] **Step 1 : Créer le contrôleur**

```java
package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.CreateVenteLibreRequest;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.GuichetRepository;
import lab.hang.Gestion.boulangerie.repository.ProductionRepository;
import lab.hang.Gestion.boulangerie.service.AppSettingsService;
import lab.hang.Gestion.boulangerie.service.UserService;
import lab.hang.Gestion.boulangerie.service.VenteLibreService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/guichet")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER','CAISSIER')")
public class GuichetController {

    private final GuichetRepository guichetRepository;
    private final ProductionRepository productionRepository;
    private final VenteLibreService venteLibreService;
    private final AppSettingsService appSettingsService;

    public GuichetController(GuichetRepository guichetRepository,
                             ProductionRepository productionRepository,
                             VenteLibreService venteLibreService,
                             AppSettingsService appSettingsService) {
        this.guichetRepository = guichetRepository;
        this.productionRepository = productionRepository;
        this.venteLibreService = venteLibreService;
        this.appSettingsService = appSettingsService;
    }

    @GetMapping
    public String selectGuichet(Model model) {
        List<Guichet> guichets = guichetRepository.findByActifTrue();
        model.addAttribute("guichets", guichets);
        return "guichet/select";
    }

    @GetMapping("/vente")
    public String pos(@RequestParam Long guichetId, Model model, RedirectAttributes ra) {
        Guichet guichet = guichetRepository.findById(guichetId)
                .orElse(null);
        if (guichet == null) return "redirect:/guichet";

        // Production du jour, sinon veille
        LocalDate today = LocalDate.now();
        List<Production> productions = productionRepository.findByDateProduction(today);
        Production production = productions.isEmpty()
                ? productionRepository.findByDateProduction(today.minusDays(1)).stream().findFirst().orElse(null)
                : productions.get(0);

        if (production == null) {
            ra.addFlashAttribute("errorMessage",
                    "Aucune production disponible aujourd'hui. Contactez le manager.");
            return "redirect:/guichet";
        }

        model.addAttribute("guichet", guichet);
        model.addAttribute("production", production);
        model.addAttribute("produitsRestants", production.getProduitsRestants());
        model.addAttribute("largeurTicketMm", appSettingsService.getLargeurTicketMm());
        model.addAttribute("moyensPaiement", MoyenPaiement.values());
        return "guichet/pos";
    }

    @PostMapping("/vente")
    public String encaisser(@ModelAttribute CreateVenteLibreRequest request,
                            @RequestParam Long guichetId,
                            RedirectAttributes ra) {
        request.setGuichetId(guichetId);
        boolean aucunProduit = request.getProduits() == null ||
                request.getProduits().values().stream().allMatch(q -> q == null || q <= 0);
        if (aucunProduit) {
            ra.addFlashAttribute("errorMessage", "Sélectionnez au moins un produit.");
            return "redirect:/guichet/vente?guichetId=" + guichetId;
        }
        try {
            venteLibreService.createVenteLibre(request);
            ra.addFlashAttribute("successMessage", "Vente enregistrée avec succès.");
            ra.addFlashAttribute("venteOk", true);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/guichet/vente?guichetId=" + guichetId;
    }
}
```

- [ ] **Step 2 : Compiler**

```bash
./mvnw compile -q
```

Expected : BUILD SUCCESS

- [ ] **Step 3 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/controller/GuichetController.java
git commit -m "feat(pos): GuichetController — select/POS/encaisser"
```

---

### Task 6 : Template sélection guichet

**Files:**
- Create: `src/main/resources/templates/guichet/select.html`

- [ ] **Step 1 : Créer select.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/layout :: head('Guichet')}"></head>
<body>
<nav th:replace="~{fragments/layout :: navbar}"></nav>
<div th:replace="~{fragments/layout :: flash-messages}"></div>

<div class="container mt-5" style="max-width:480px">
    <div class="text-center mb-4">
        <div class="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3"
             style="width:64px;height:64px">
            <i class="fas fa-cash-register fa-xl text-primary"></i>
        </div>
        <h1 class="h4 fw-bold mb-1">Ouvrir le guichet</h1>
        <p class="text-muted small">Choisissez votre guichet pour commencer la vente</p>
    </div>

    <div th:if="${#lists.isEmpty(guichets)}" class="alert alert-warning text-center">
        <i class="fas fa-exclamation-triangle me-2"></i>
        Aucun guichet actif. Contactez votre administrateur.
    </div>

    <div th:unless="${#lists.isEmpty(guichets)}" class="d-grid gap-3">
        <a th:each="g : ${guichets}"
           th:href="@{/guichet/vente(guichetId=${g.id})}"
           class="btn btn-outline-primary btn-lg d-flex align-items-center justify-content-between px-4 py-3">
            <span>
                <i class="fas fa-store me-2"></i>
                <span th:text="${g.nom}"></span>
            </span>
            <span class="badge bg-primary" th:text="${g.pointDeVente.nom}"></span>
        </a>
    </div>

    <div class="mt-4 text-center">
        <a href="/dashboard" class="btn btn-outline-secondary btn-sm">
            <i class="fas fa-arrow-left me-1"></i>Retour au tableau de bord
        </a>
    </div>
</div>

<div th:replace="~{fragments/layout :: scripts}"></div>
</body>
</html>
```

- [ ] **Step 2 : Commit**

```bash
git add src/main/resources/templates/guichet/select.html
git commit -m "feat(pos): page sélection guichet"
```

---

### Task 7 : Template POS (tuiles + JS + encaissement)

**Files:**
- Create: `src/main/resources/templates/guichet/pos.html`

- [ ] **Step 1 : Créer pos.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head th:replace="~{fragments/layout :: head('Caisse')}"></head>
<body style="background:#f0f2f5">
<nav th:replace="~{fragments/layout :: navbar}"></nav>

<!-- En-tête guichet -->
<div class="bg-white border-bottom px-3 py-2 d-flex justify-content-between align-items-center">
    <div>
        <span class="fw-bold"><i class="fas fa-cash-register me-2 text-primary"></i>
            <span th:text="${guichet.nom}"></span>
        </span>
        <span class="text-muted small ms-2" th:text="${guichet.pointDeVente.nom}"></span>
    </div>
    <div class="d-flex align-items-center gap-3">
        <span class="text-muted small" th:text="${#temporals.format(#temporals.createNow(),'dd/MM/yyyy')}"></span>
        <a th:href="@{/guichet}" class="btn btn-outline-secondary btn-sm">
            <i class="fas fa-exchange-alt me-1"></i>Changer
        </a>
    </div>
</div>

<!-- Flash messages -->
<div th:if="${successMessage}" class="alert alert-success alert-dismissible m-3 mb-0" role="alert">
    <i class="fas fa-check-circle me-2"></i><span th:text="${successMessage}"></span>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
<div th:if="${errorMessage}" class="alert alert-danger alert-dismissible m-3 mb-0" role="alert">
    <i class="fas fa-exclamation-circle me-2"></i><span th:text="${errorMessage}"></span>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>

<div class="container-fluid px-3 py-3">

    <!-- Formulaire caché -->
    <form id="posForm" th:action="@{/guichet/vente}" method="post">
        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
        <input type="hidden" name="guichetId" th:value="${guichet.id}"/>
        <input type="hidden" name="productionId" th:value="${production.id}"/>
        <input type="hidden" name="moyenPaiement" id="moyenPaiementInput" value="ESPECES"/>
        <!-- Champs quantités générés dynamiquement par JS -->
    </form>

    <!-- Grille de tuiles -->
    <div class="row g-3 mb-3">
        <div th:each="entry : ${produitsRestants}" class="col-6 col-md-4 col-lg-3">
            <div th:with="produit=${entry.key}, stock=${entry.value}"
                 th:class="${stock <= 0} ? 'tile-produit tile-epuise' :
                            (${stock <= 5} ? 'tile-produit tile-faible' : 'tile-produit tile-dispo')"
                 th:data-id="${produit.id}"
                 th:data-prix="${produit.prix}"
                 th:data-stock="${stock}"
                 th:data-nom="${produit.nom}">
                <div class="tile-icon">🍞</div>
                <div class="tile-nom" th:text="${produit.nom}"></div>
                <div class="tile-prix" th:text="${#numbers.formatDecimal(produit.prix,1,'COMMA',0,'POINT') + ' XAF'}"></div>
                <div th:if="${stock > 0}" class="tile-stock"
                     th:text="${stock <= 5} ? (${stock} + ' restants ⚠') : (${stock} + ' restants')"></div>
                <div th:if="${stock <= 0}" class="tile-stock">Épuisé</div>
                <div th:if="${stock > 0}" class="tile-controls">
                    <button type="button" class="btn-moins" th:onclick="'ajuster(' + ${produit.id} + ', -1)'">−</button>
                    <span class="tile-qty" th:id="'qty-' + ${produit.id}">0</span>
                    <button type="button" class="btn-plus" th:onclick="'ajuster(' + ${produit.id} + ', 1)'">+</button>
                </div>
            </div>
        </div>

        <div th:if="${#maps.isEmpty(produitsRestants)}" class="col-12">
            <div class="alert alert-warning">
                <i class="fas fa-exclamation-triangle me-2"></i>
                Aucun produit disponible pour cette production.
            </div>
        </div>
    </div>

    <!-- Barre du bas -->
    <div class="pos-footer">
        <div class="paiement-buttons">
            <span class="text-muted small d-block mb-2">Paiement</span>
            <button type="button" id="btnEspeces" class="btn btn-primary btn-sm px-3"
                    onclick="setPaiement('ESPECES')">
                <i class="fas fa-money-bill me-1"></i>Espèces
            </button>
            <button type="button" id="btnMobile" class="btn btn-outline-secondary btn-sm px-3"
                    onclick="setPaiement('MOBILE_MONEY')">
                <i class="fas fa-mobile-alt me-1"></i>Mobile Money
            </button>
        </div>
        <div class="total-section text-end">
            <div class="text-muted small">Total</div>
            <div class="total-montant" id="totalMontant">0 XAF</div>
            <div class="text-muted small" id="totalDetail"></div>
        </div>
        <button type="button" id="btnEncaisser" class="btn btn-success btn-lg fw-bold px-4" disabled
                onclick="soumettre()">
            <i class="fas fa-check me-2"></i>ENCAISSER
        </button>
    </div>

    <!-- Bouton imprimer (visible après vente réussie) -->
    <div th:if="${venteOk}" class="text-center mt-3">
        <button onclick="window.print()" class="btn btn-outline-secondary">
            <i class="fas fa-print me-2"></i>Imprimer le ticket
        </button>
    </div>

</div>

<!-- Ticket d'impression (inclus mais masqué à l'écran) -->
<div th:replace="~{guichet/ticket-fragment :: ticket(guichet=${guichet}, production=${production}, largeur=${largeurTicketMm})}"></div>

<div th:replace="~{fragments/layout :: scripts}"></div>

<style>
.tile-produit {
    border-radius: 12px; padding: 16px; text-align: center;
    cursor: pointer; color: white; box-shadow: 0 2px 8px rgba(0,0,0,.15);
    min-height: 140px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px;
}
.tile-dispo  { background: #0d6efd; }
.tile-faible { background: #dc3545; }
.tile-epuise { background: #e9ecef; color: #adb5bd; cursor: default; }
.tile-icon   { font-size: 28px; }
.tile-nom    { font-weight: 700; font-size: 14px; }
.tile-prix   { font-size: 12px; opacity: .85; }
.tile-stock  { font-size: 11px; opacity: .75; }
.tile-controls { display: flex; align-items: center; gap: 12px; margin-top: 8px; }
.btn-moins, .btn-plus {
    background: rgba(0,0,0,.25); border: none; border-radius: 6px;
    width: 28px; height: 28px; color: white; font-size: 18px; cursor: pointer; line-height: 1;
}
.btn-plus { background: rgba(255,255,255,.3); }
.tile-qty  { font-size: 20px; font-weight: 900; min-width: 24px; }
.pos-footer {
    position: sticky; bottom: 0; background: white; border-radius: 12px;
    padding: 14px 16px; display: flex; align-items: center; gap: 16px;
    box-shadow: 0 -2px 12px rgba(0,0,0,.1);
}
.paiement-buttons { flex: 1; display: flex; flex-direction: column; }
.total-section { flex: 1; }
.total-montant { font-size: 26px; font-weight: 900; color: #198754; }
@media print {
    nav, .pos-footer, .paiement-buttons, #btnEncaisser, .alert, .btn, .container-fluid { display: none !important; }
    #ticket-caisse { display: block !important; }
}
</style>

<script th:inline="javascript">
const stocks = {};
const prix = {};
const noms = {};
const qtys = {};

// Initialiser depuis les tuiles
document.querySelectorAll('.tile-produit[data-id]').forEach(tile => {
    const id = tile.dataset.id;
    stocks[id] = parseInt(tile.dataset.stock);
    prix[id]   = parseFloat(tile.dataset.prix);
    noms[id]   = tile.dataset.nom;
    qtys[id]   = 0;
});

function ajuster(id, delta) {
    const sid = String(id);
    const nouveau = (qtys[sid] || 0) + delta;
    if (nouveau < 0 || nouveau > stocks[sid]) return;
    qtys[sid] = nouveau;
    document.getElementById('qty-' + sid).textContent = nouveau;
    mettreAJourTotal();
}

function mettreAJourTotal() {
    let total = 0;
    const lignes = [];
    for (const [id, qty] of Object.entries(qtys)) {
        if (qty > 0) {
            total += qty * prix[id];
            lignes.push(qty + ' × ' + noms[id]);
        }
    }
    const fmt = v => v.toLocaleString('fr-FR') + ' XAF';
    document.getElementById('totalMontant').textContent = fmt(total);
    document.getElementById('totalDetail').textContent = lignes.join(' · ');
    document.getElementById('btnEncaisser').disabled = (total === 0);
    // Mettre à jour les champs cachés dans le formulaire
    mettreAJourFormulaire();
}

function mettreAJourFormulaire() {
    // Supprimer les anciens champs produits
    document.querySelectorAll('#posForm input[name^="produits"]').forEach(e => e.remove());
    const form = document.getElementById('posForm');
    for (const [id, qty] of Object.entries(qtys)) {
        if (qty > 0) {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'produits[' + id + ']';
            input.value = qty;
            form.appendChild(input);
        }
    }
}

function setPaiement(mode) {
    document.getElementById('moyenPaiementInput').value = mode;
    document.getElementById('btnEspeces').className = mode === 'ESPECES'
        ? 'btn btn-primary btn-sm px-3' : 'btn btn-outline-secondary btn-sm px-3';
    document.getElementById('btnMobile').className = mode === 'MOBILE_MONEY'
        ? 'btn btn-primary btn-sm px-3' : 'btn btn-outline-secondary btn-sm px-3';
    // Mise à jour ticket
    document.getElementById('ticketPaiement').textContent =
        mode === 'MOBILE_MONEY' ? 'Mobile Money' : 'Espèces';
}

function soumettre() {
    mettreAJourFormulaire();
    document.getElementById('posForm').submit();
}
</script>
</body>
</html>
```

- [ ] **Step 2 : Commit**

```bash
git add src/main/resources/templates/guichet/pos.html
git commit -m "feat(pos): interface POS tuiles avec JS compteur et encaissement"
```

---

### Task 8 : Fragment ticket thermique

**Files:**
- Create: `src/main/resources/templates/guichet/ticket-fragment.html`

- [ ] **Step 1 : Créer ticket-fragment.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<div th:fragment="ticket(guichet, production, largeur)" id="ticket-caisse"
     style="display:none; font-family:monospace; font-size:12px; width:100%">

    <style th:inline="text">
        @media print {
            @page { size: [[${largeur}]]mm auto; margin: 3mm; }
            #ticket-caisse { display: block !important; }
        }
    </style>

    <div style="text-align:center; border-bottom: 1px dashed #000; padding-bottom:6px; margin-bottom:6px">
        <div style="font-weight:bold; font-size:14px" th:text="${appName ?: 'Gestiboul'}"></div>
        <div th:text="${guichet.pointDeVente.nom}"></div>
    </div>

    <div style="margin-bottom:4px">
        <span>Guichet : </span><span th:text="${guichet.nom}"></span><br>
        <span>Date    : </span><span th:text="${#temporals.format(#temporals.createNow(),'dd/MM/yyyy HH:mm')}"></span><br>
        <span>Caissier: </span><span sec:authentication="principal.username"></span>
    </div>

    <div style="border-top:1px dashed #000; border-bottom:1px dashed #000; padding:4px 0; margin:4px 0" id="ticketLignes">
        <!-- Rempli dynamiquement par JS après encaissement -->
    </div>

    <div style="font-weight:bold; display:flex; justify-content:space-between">
        <span>TOTAL</span>
        <span id="ticketTotal">—</span>
    </div>
    <div>Paiement : <span id="ticketPaiement">Espèces</span></div>

    <div style="text-align:center; border-top:1px dashed #000; margin-top:6px; padding-top:6px">
        <div>Merci pour votre achat !</div>
        <div style="font-size:10px">Gestiboul — Hanga J.F.</div>
    </div>
</div>

<script>
// Mise à jour du ticket après chaque changement de quantité
function mettreAJourTicket() {
    const lignes = [];
    let total = 0;
    for (const [id, qty] of Object.entries(window.qtys || {})) {
        if (qty > 0) {
            const montant = qty * (window.prix[id] || 0);
            total += montant;
            lignes.push(
                (window.noms[id] || '?').padEnd(12).substring(0,12) +
                ' x' + qty + '  ' +
                montant.toLocaleString('fr-FR') + ' XAF'
            );
        }
    }
    const el = document.getElementById('ticketLignes');
    if (el) {
        el.textContent = '';
        lignes.forEach(l => {
            const div = document.createElement('div');
            div.textContent = l;
            el.appendChild(div);
        });
    }
    const tot = document.getElementById('ticketTotal');
    if (tot) tot.textContent = total.toLocaleString('fr-FR') + ' XAF';
}
// Brancher sur mettreAJourTotal existant
const _origMAJ = window.mettreAJourTotal;
if (_origMAJ) {
    window.mettreAJourTotal = function() { _origMAJ(); mettreAJourTicket(); };
}
</script>
</body>
</html>
```

- [ ] **Step 2 : Commit**

```bash
git add src/main/resources/templates/guichet/ticket-fragment.html
git commit -m "feat(pos): fragment ticket thermique CSS @media print"
```

---

### Task 9 : Navbar CAISSIER + lien guichet

**Files:**
- Modify: `src/main/resources/templates/fragments/layout.html`

- [ ] **Step 1 : Ajouter le lien "Guichet" dans la navbar**

Dans `layout.html`, dans la liste `<ul class="navbar-nav me-auto ...">`, après le lien "Accueil", ajouter :

```html
<li class="nav-item" sec:authorize="hasAnyRole('CAISSIER','ADMIN','MANAGER')">
    <a class="nav-link rounded px-2" href="/guichet">
        <i class="fas fa-cash-register me-1"></i>Guichet
    </a>
</li>
```

- [ ] **Step 2 : Compiler et lancer tous les tests**

```bash
./mvnw test -q 2>&1 | tail -10
```

Expected : BUILD SUCCESS, tous les tests passent

- [ ] **Step 3 : Commit**

```bash
git add src/main/resources/templates/fragments/layout.html
git commit -m "feat(pos): lien Guichet dans navbar pour CAISSIER/ADMIN/MANAGER"
```

---

### Task 10 : Test de compilation finale + vérification manuelle

- [ ] **Step 1 : Build complet**

```bash
./mvnw package -DskipTests -q
```

Expected : BUILD SUCCESS, JAR généré dans `target/`

- [ ] **Step 2 : Démarrer l'app**

```bash
./mvnw spring-boot:run
```

Attendre `Started GestionBoulangerieApplication`.

- [ ] **Step 3 : Test login caissier1**

Ouvrir `http://localhost:9000/login`, se connecter avec `caissier1 / caisse2024`.

Expected :
- Dashboard visible avec lien "Guichet" dans la navbar
- Pas de lien Commandes, Production, Finances, etc.

- [ ] **Step 4 : Test sélection guichet**

Cliquer "Guichet" → `http://localhost:9000/guichet`

Expected : liste des guichets actifs (ex. Guichet 1 — Point de vente Centre)

- [ ] **Step 5 : Test interface POS**

Cliquer sur un guichet → `/guichet/vente?guichetId=1`

Expected :
- Tuiles des produits disponibles avec +/−
- Total = 0 XAF, bouton ENCAISSER grisé
- Ajuster une quantité → total mis à jour, bouton activé

- [ ] **Step 6 : Test encaissement**

Sélectionner 2 Baguettes, choisir "Mobile Money", cliquer ENCAISSER.

Expected :
- Message vert "Vente enregistrée avec succès"
- Bouton "Imprimer le ticket" visible
- POS rechargé avec stocks réduits

- [ ] **Step 7 : Test impression ticket**

Cliquer "Imprimer le ticket" → boîte de dialogue d'impression OS.

Expected : aperçu du ticket au format monospace avec produits, total, "Mobile Money"

- [ ] **Step 8 : Test accès refusé caissier → /ventes-libres**

Depuis la session caissier1, tenter `http://localhost:9000/ventes-libres`

Expected : redirection vers /dashboard (AccessDeniedException intercepté)

- [ ] **Step 9 : Test paramètre largeur ticket**

Se connecter en `hanga / Hanga@Gestiboul2024`, aller dans `/admin/settings`, changer la largeur à 58mm, valider.

Expected : message de confirmation "Largeur ticket mise à jour : 58 mm"

- [ ] **Step 10 : Commit final**

```bash
git add -A
git commit -m "feat(pos): guichet POS caissier complet — tuiles, encaissement, ticket thermique"
```
