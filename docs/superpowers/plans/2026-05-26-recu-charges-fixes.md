# Reçus de Paiement des Charges Fixes — Plan d'implémentation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transformer le paiement d'une charge fixe en flux complet : formulaire avec choix du compte bancaire, création d'une Transaction, débit du solde, et génération d'un reçu PDF téléchargeable.

**Architecture:** On modifie l'entité `ChargeFixe` (deux nouveaux champs), on enrichit `ChargeFixeService` (inject TransactionRepository + CompteBancaireRepository, nouvelle signature `payerChargeFixe(id, compteBancaireId)`), on remplace le GET direct en formulaire GET/POST dans `ComptabiliteController`, et on ajoute une route PDF dans le `PdfController` existant.

**Tech Stack:** Spring Boot 3.4.2, JPA/Hibernate, Thymeleaf, Bootstrap 5, Flying Saucer (ITextRenderer), JUnit 5 + Mockito

---

## File Map

| Action | Fichier |
|--------|---------|
| Modify | `src/main/java/lab/hang/Gestion/boulangerie/model/ChargeFixe.java` |
| Modify | `src/main/java/lab/hang/Gestion/boulangerie/service/ChargeFixeService.java` |
| Create | `src/test/java/lab/hang/gestion_boulangerie/service/ChargeFixeServiceTest.java` |
| Modify | `src/main/java/lab/hang/Gestion/boulangerie/controller/ComptabiliteController.java` |
| Modify | `src/main/java/lab/hang/Gestion/boulangerie/controller/PdfController.java` |
| Create | `src/main/resources/templates/comptabilite/payer-charge.html` |
| Modify | `src/main/resources/templates/comptabilite/charges-fixes.html` |
| Create | `src/main/resources/templates/comptabilite/recu-charge-template.html` |

---

## Task 1 : ChargeFixe entity — datePaiement + transaction

**Files:**
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/model/ChargeFixe.java`

- [ ] **Step 1 : Ajouter les champs `datePaiement` et `transaction`**

Dans `ChargeFixe.java`, après le champ `periodicite`, ajouter :

```java
private LocalDate datePaiement;

@OneToOne(optional = true)
private Transaction transaction;
```

Ajouter les imports en tête de fichier :
```java
import lab.hang.Gestion.boulangerie.model.Transaction;
```

(`LocalDate` est déjà importé.)

- [ ] **Step 2 : Ajouter les getters/setters**

Après les getters/setters existants, ajouter :

```java
public LocalDate getDatePaiement() {
    return datePaiement;
}

public void setDatePaiement(LocalDate datePaiement) {
    this.datePaiement = datePaiement;
}

public Transaction getTransaction() {
    return transaction;
}

public void setTransaction(Transaction transaction) {
    this.transaction = transaction;
}
```

- [ ] **Step 3 : Compiler**

```
.\mvnw.cmd compile -q
```
Attendu : BUILD SUCCESS

- [ ] **Step 4 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/model/ChargeFixe.java
git commit -m "feat(charges): ajoute datePaiement et transaction a ChargeFixe"
```

---

## Task 2 : ChargeFixeService — tests + logique de paiement + méthodes utilitaires

**Files:**
- Create: `src/test/java/lab/hang/gestion_boulangerie/service/ChargeFixeServiceTest.java`
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/service/ChargeFixeService.java`

### Contexte : structure actuelle de `ChargeFixeService`

Le service injecte actuellement `ChargeFixeRepository` et `AlerteService`. Il utilise `@jakarta.transaction.Transactional` sur les méthodes d'écriture. La méthode `payerChargeFixe(Long id)` ne crée pas de Transaction ni ne débite de compte — c'est ce qu'on va corriger.

- [ ] **Step 1 : Écrire les tests (échouent pour l'instant)**

Créer `src/test/java/lab/hang/gestion_boulangerie/service/ChargeFixeServiceTest.java` :

```java
package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.ChargeFixe;
import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.repository.AlerteRepository;
import lab.hang.Gestion.boulangerie.repository.ChargeFixeRepository;
import lab.hang.Gestion.boulangerie.repository.CompteBancaireRepository;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import lab.hang.Gestion.boulangerie.service.AlerteService;
import lab.hang.Gestion.boulangerie.service.ChargeFixeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargeFixeServiceTest {

    @Mock ChargeFixeRepository chargeFixeRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock CompteBancaireRepository compteBancaireRepository;
    @Mock AlerteService alerteService;
    @InjectMocks ChargeFixeService chargeFixeService;

    private ChargeFixe charge(boolean paye) {
        ChargeFixe c = new ChargeFixe();
        c.setId(1L);
        c.setType("LOYER");
        c.setDescription("Loyer local boulangerie");
        c.setMontant(150_000.0);
        c.setDateEcheance(LocalDate.of(2026, 6, 1));
        c.setPaye(paye);
        return c;
    }

    @Test
    void payerChargeFixe_createsTransactionAndUpdatesCharge() {
        ChargeFixe c = charge(false);
        CompteBancaire compte = new CompteBancaire();
        compte.setId(2L);
        compte.setNom("Compte Principal");
        compte.setSolde(500_000.0);

        when(chargeFixeRepository.findById(1L)).thenReturn(Optional.of(c));
        when(compteBancaireRepository.findById(2L)).thenReturn(Optional.of(compte));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(chargeFixeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        chargeFixeService.payerChargeFixe(1L, 2L);

        assertThat(c.isPaye()).isTrue();
        assertThat(c.getDatePaiement()).isEqualTo(LocalDate.now());
        assertThat(c.getTransaction()).isNotNull();
        assertThat(compte.getSolde()).isCloseTo(350_000.0, within(0.01));

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction tx = txCaptor.getValue();
        assertThat(tx.getType()).isEqualTo("CHARGE");
        assertThat(tx.getMontant()).isCloseTo(150_000.0, within(0.01));
        assertThat(tx.getDescription()).contains("LOYER").contains("Loyer local boulangerie");
        assertThat(tx.getCompteBancaire()).isEqualTo(compte);
    }

    @Test
    void payerChargeFixe_throwsWhenAlreadyPaid() {
        when(chargeFixeRepository.findById(1L)).thenReturn(Optional.of(charge(true)));

        assertThatThrownBy(() -> chargeFixeService.payerChargeFixe(1L, 2L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("déjà payée");
    }

    @Test
    void payerChargeFixe_throwsWhenCompteNotFound() {
        when(chargeFixeRepository.findById(1L)).thenReturn(Optional.of(charge(false)));
        when(compteBancaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeFixeService.payerChargeFixe(1L, 99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(chargeFixeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeFixeService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
```

- [ ] **Step 2 : Vérifier que les tests échouent**

```
.\mvnw.cmd test -Dtest=ChargeFixeServiceTest -q 2>&1 | Select-Object -Last 5
```
Attendu : FAILURE (méthodes introuvables)

- [ ] **Step 3 : Modifier `ChargeFixeService`**

Remplacer **entièrement** le contenu de `ChargeFixeService.java` par :

```java
package lab.hang.Gestion.boulangerie.service;

import jakarta.transaction.Transactional;
import lab.hang.Gestion.boulangerie.dto.ChargeFixeDTO;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.ChargeFixe;
import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.repository.ChargeFixeRepository;
import lab.hang.Gestion.boulangerie.repository.CompteBancaireRepository;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChargeFixeService {

    private final ChargeFixeRepository chargeFixeRepository;
    private final TransactionRepository transactionRepository;
    private final CompteBancaireRepository compteBancaireRepository;
    private final AlerteService alerteService;

    public ChargeFixeService(ChargeFixeRepository chargeFixeRepository,
                             TransactionRepository transactionRepository,
                             CompteBancaireRepository compteBancaireRepository,
                             AlerteService alerteService) {
        this.chargeFixeRepository = chargeFixeRepository;
        this.transactionRepository = transactionRepository;
        this.compteBancaireRepository = compteBancaireRepository;
        this.alerteService = alerteService;
    }

    public ChargeFixe getById(Long id) {
        return chargeFixeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Charge fixe non trouvée : " + id));
    }

    @Transactional
    public ChargeFixe getByIdForPdf(Long id) {
        ChargeFixe c = chargeFixeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Charge fixe non trouvée : " + id));
        // Initialise le proxy lazy Transaction.compteBancaire dans la transaction
        if (c.getTransaction() != null) {
            c.getTransaction().getCompteBancaire().getNom();
        }
        return c;
    }

    public List<CompteBancaire> getAllComptesBancaires() {
        return compteBancaireRepository.findAll();
    }

    @Transactional
    public void payerChargeFixe(Long id, Long compteBancaireId) {
        ChargeFixe charge = chargeFixeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Charge fixe non trouvée : " + id));

        if (charge.isPaye()) {
            throw new IllegalStateException("Cette charge est déjà payée.");
        }

        CompteBancaire compte = compteBancaireRepository.findById(compteBancaireId)
            .orElseThrow(() -> new ResourceNotFoundException("Compte bancaire introuvable : " + compteBancaireId));

        Transaction transaction = new Transaction();
        transaction.setType("CHARGE");
        transaction.setMontant(charge.getMontant());
        transaction.setDate(LocalDate.now());
        transaction.setDescription("Paiement " + charge.getType() + " — " + charge.getDescription());
        transaction.setCompteBancaire(compte);

        compte.setSolde(compte.getSolde() - charge.getMontant());
        compteBancaireRepository.save(compte);
        Transaction savedTx = transactionRepository.save(transaction);

        charge.setPaye(true);
        charge.setDatePaiement(LocalDate.now());
        charge.setTransaction(savedTx);

        if (charge.getPeriodicite() != null) {
            creerProchaineEcheance(charge);
        }

        chargeFixeRepository.save(charge);
    }

    public List<ChargeFixeDTO> getChargesFixesAVenir(int joursAvant) {
        LocalDate dateDebut = LocalDate.now();
        LocalDate dateFin = dateDebut.plusDays(joursAvant);
        return chargeFixeRepository.findByDateEcheanceBetween(dateDebut, dateFin)
            .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void creerChargeFixe(ChargeFixeDTO chargeFixeDTO) {
        chargeFixeRepository.save(mapToEntity(chargeFixeDTO));
    }

    public List<ChargeFixeDTO> getAllChargesFixe() {
        return chargeFixeRepository.findAll().stream()
            .map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ChargeFixeDTO> getBilanChargesFixes() {
        return chargeFixeRepository.findAll().stream()
            .filter(ChargeFixe::isPaye).map(this::mapToDTO).collect(Collectors.toList());
    }

    private void creerProchaineEcheance(ChargeFixe charge) {
        ChargeFixe nouvelleCharge = new ChargeFixe();
        nouvelleCharge.setType(charge.getType());
        nouvelleCharge.setMontant(charge.getMontant());
        nouvelleCharge.setDescription(charge.getDescription());
        nouvelleCharge.setPeriodicite(charge.getPeriodicite());

        LocalDate prochaineEcheance = switch (charge.getPeriodicite()) {
            case "MENSUEL"     -> charge.getDateEcheance().plusMonths(1);
            case "TRIMESTRIEL" -> charge.getDateEcheance().plusMonths(3);
            case "ANNUEL"      -> charge.getDateEcheance().plusYears(1);
            default            -> null;
        };

        if (prochaineEcheance != null) {
            nouvelleCharge.setDateEcheance(prochaineEcheance);
            nouvelleCharge.setPaye(false);
            chargeFixeRepository.save(nouvelleCharge);
            alerteService.creerAlerte(
                "CHARGE_FIXE", "INFO",
                "Nouvelle échéance créée pour " + charge.getType() + " le " + prochaineEcheance
            );
        }
    }

    private ChargeFixeDTO mapToDTO(ChargeFixe c) {
        ChargeFixeDTO dto = new ChargeFixeDTO();
        dto.setId(c.getId());
        dto.setType(c.getType());
        dto.setMontant(c.getMontant());
        dto.setDescription(c.getDescription());
        dto.setPeriodicite(c.getPeriodicite());
        dto.setDateEcheance(c.getDateEcheance());
        dto.setPaye(c.isPaye());
        return dto;
    }

    private ChargeFixe mapToEntity(ChargeFixeDTO dto) {
        ChargeFixe c = new ChargeFixe();
        c.setType(dto.getType());
        c.setMontant(dto.getMontant());
        c.setDescription(dto.getDescription());
        c.setPeriodicite(dto.getPeriodicite());
        c.setDateEcheance(dto.getDateEcheance());
        c.setPaye(dto.isPaye());
        return c;
    }
}
```

- [ ] **Step 4 : Vérifier que les tests passent**

```
.\mvnw.cmd test -Dtest=ChargeFixeServiceTest -q 2>&1 | Select-Object -Last 5
```
Attendu : BUILD SUCCESS, Tests run: 4, Failures: 0

- [ ] **Step 5 : Vérifier qu'il n'y a pas de régression**

```
.\mvnw.cmd test -q 2>&1 | Select-String "Tests run|BUILD" | Select-Object -Last 5
```
Attendu : BUILD SUCCESS

- [ ] **Step 6 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/service/ChargeFixeService.java
git add src/test/java/lab/hang/gestion_boulangerie/service/ChargeFixeServiceTest.java
git commit -m "feat(charges): paiement avec Transaction + debit compte + recu PDF ready"
```

---

## Task 3 : ComptabiliteController + templates

**Files:**
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/controller/ComptabiliteController.java`
- Create: `src/main/resources/templates/comptabilite/payer-charge.html`
- Modify: `src/main/resources/templates/comptabilite/charges-fixes.html`

### Contexte : état actuel du contrôleur

`ComptabiliteController` injecte `ChargeFixeService`, `FacturationService`, `KPIService`, `AlerteService`. Le `GET /charges-fixes/{id}/payer` appelle directement `chargeFixeService.payerChargeFixe(id)` et redirige. Il faut :
1. Transformer ce GET en formulaire
2. Ajouter le POST de traitement

- [ ] **Step 1 : Modifier `ComptabiliteController`**

Remplacer la méthode `payerChargeFixe` existante (lignes 99–108) par :

```java
@GetMapping("/charges-fixes/{id}/payer")
public String showPayerChargeForm(@PathVariable Long id, Model model) {
    model.addAttribute("charge", chargeFixeService.getById(id));
    model.addAttribute("comptes", chargeFixeService.getAllComptesBancaires());
    return "comptabilite/payer-charge";
}

@PostMapping("/charges-fixes/{id}/payer")
public String payerChargeFixe(@PathVariable Long id,
                               @RequestParam Long compteBancaireId,
                               RedirectAttributes ra) {
    try {
        chargeFixeService.payerChargeFixe(id, compteBancaireId);
        ra.addFlashAttribute("success", "Paiement enregistré avec succès.");
    } catch (Exception e) {
        ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
    }
    return "redirect:/comptabilite/charges-fixes";
}
```

Ajouter l'import manquant si absent :
```java
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
```

- [ ] **Step 2 : Créer `src/main/resources/templates/comptabilite/payer-charge.html`**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/layout :: head('Payer une charge')}"></head>
<body>
<nav th:replace="~{fragments/layout :: navbar}"></nav>
<div th:replace="~{fragments/layout :: flash-messages}"></div>

<div class="container mt-4" style="max-width:640px">
    <div class="mb-4">
        <h1 class="h4 fw-bold mb-0">
            <i class="fas fa-money-check-alt me-2 text-primary"></i>Payer une charge fixe
        </h1>
    </div>

    <!-- Résumé de la charge -->
    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <dl class="row mb-0">
                <dt class="col-sm-4">Type</dt>
                <dd class="col-sm-8"><span class="badge bg-secondary" th:text="${charge.type}"></span></dd>
                <dt class="col-sm-4">Description</dt>
                <dd class="col-sm-8" th:text="${charge.description}"></dd>
                <dt class="col-sm-4">Montant</dt>
                <dd class="col-sm-8 fw-bold"
                    th:text="${#numbers.formatDecimal(charge.montant,1,'COMMA',0,'POINT') + ' XAF'}"></dd>
                <dt class="col-sm-4">&#201;ch&#233;ance</dt>
                <dd class="col-sm-8" th:text="${#temporals.format(charge.dateEcheance,'dd/MM/yyyy')}"></dd>
            </dl>
        </div>
    </div>

    <!-- Formulaire -->
    <div class="card shadow-sm">
        <div class="card-body">
            <form th:action="@{/comptabilite/charges-fixes/{id}/payer(id=${charge.id})}" method="post">
                <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>

                <div class="mb-3">
                    <label class="form-label fw-semibold">
                        Compte &#224; d&#233;biter <span class="text-danger">*</span>
                    </label>
                    <select name="compteBancaireId" id="compteSelect" class="form-select" required
                            onchange="verifierSolde(this)">
                        <option value="">&#8212; S&#233;lectionner &#8212;</option>
                        <option th:each="c : ${comptes}"
                                th:value="${c.id}"
                                th:text="${c.nom + ' — ' + #numbers.formatDecimal(c.solde,1,'COMMA',0,'POINT') + ' XAF'}"
                                th:data-solde="${c.solde}">
                        </option>
                    </select>
                    <div id="soldeAlert" class="alert alert-warning mt-2 d-none">
                        <i class="fas fa-exclamation-triangle me-1"></i>
                        Solde insuffisant pour couvrir ce paiement.
                    </div>
                </div>

                <div class="d-flex gap-2">
                    <button type="submit" class="btn btn-success">
                        <i class="fas fa-check me-1"></i>Confirmer le paiement
                    </button>
                    <a href="/comptabilite/charges-fixes" class="btn btn-outline-secondary">Annuler</a>
                </div>
            </form>
        </div>
    </div>
</div>

<div th:replace="~{fragments/layout :: scripts}"></div>
<script th:inline="javascript">
    const montant = [[${charge.montant}]];
    function verifierSolde(sel) {
        const opt = sel.options[sel.selectedIndex];
        const solde = parseFloat(opt.getAttribute('data-solde') || 'Infinity');
        document.getElementById('soldeAlert').classList.toggle('d-none', isNaN(solde) || solde >= montant);
    }
</script>
</body>
</html>
```

- [ ] **Step 3 : Modifier la colonne Actions dans `charges-fixes.html`**

Remplacer le bloc `<td>` des actions (lignes 106–113) par :

```html
<td>
    <div class="d-flex gap-1">
        <a th:if="${!charge.paye}"
           th:href="@{/comptabilite/charges-fixes/{id}/payer(id=${charge.id})}"
           class="btn btn-sm btn-success">
            <i class="fas fa-check me-1"></i>Payer
        </a>
        <a th:if="${charge.paye}"
           th:href="@{/comptabilite/charges-fixes/{id}/recu(id=${charge.id})}"
           class="btn btn-sm btn-outline-secondary" title="T&#233;l&#233;charger le re&#231;u">
            <i class="fas fa-file-pdf"></i>
        </a>
    </div>
</td>
```

- [ ] **Step 4 : Compiler**

```
.\mvnw.cmd compile -q
```
Attendu : BUILD SUCCESS

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/controller/ComptabiliteController.java
git add src/main/resources/templates/comptabilite/payer-charge.html
git add src/main/resources/templates/comptabilite/charges-fixes.html
git commit -m "feat(charges): formulaire de paiement avec choix du compte et bouton recu"
```

---

## Task 4 : PdfController + template recu-charge-template.html

**Files:**
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/controller/PdfController.java`
- Create: `src/main/resources/templates/comptabilite/recu-charge-template.html`

### Contexte : PdfController existant

`PdfController` injecte déjà `TemplateEngine`, `ITextRenderer`, `ClassPathResource("static/fonts/arial.ttf")`, `addBrandToContext()`. Il faut ajouter `ChargeFixeService` comme champ, l'injecter dans le constructeur, et ajouter la nouvelle route. Le constructeur actuel accepte 14 paramètres — on en ajoute un.

- [ ] **Step 1 : Ajouter `ChargeFixeService` dans `PdfController`**

Ajouter le champ après `EmployeService employeService` (ligne ~55) :

```java
private final ChargeFixeService chargeFixeService;
```

Ajouter l'import :
```java
import lab.hang.Gestion.boulangerie.service.ChargeFixeService;
import lab.hang.Gestion.boulangerie.model.ChargeFixe;
```

Modifier le constructeur — ajouter après `EmployeService employeService` :

```java
                              ChargeFixeService chargeFixeService
```

Et dans le corps du constructeur, après `this.employeService = employeService;` :

```java
        this.chargeFixeService = chargeFixeService;
```

- [ ] **Step 2 : Ajouter la route `GET /comptabilite/charges-fixes/{id}/recu`**

Ajouter la méthode suivante après `getBulletinPdf()` :

```java
    @GetMapping("/comptabilite/charges-fixes/{id}/recu")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void getRecuChargePdf(@PathVariable Long id, HttpServletResponse response) throws Exception {
        ChargeFixe charge = chargeFixeService.getByIdForPdf(id);

        if (!charge.isPaye()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Cette charge n'est pas encore payée — reçu indisponible.");
            return;
        }

        Context context = new Context();
        context.setVariable("charge", charge);
        addBrandToContext(context);

        String html = templateEngine.process("comptabilite/recu-charge-template", context);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
            "attachment; filename=recu-charge-" + charge.getId()
            + "-" + charge.getDatePaiement() + ".pdf");

        try (OutputStream outputStream = response.getOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            ClassPathResource fontResource = new ClassPathResource("static/fonts/arial.ttf");
            renderer.getFontResolver().addFont(
                fontResource.getFile().getAbsolutePath(),
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED
            );
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
        }
    }
```

- [ ] **Step 3 : Créer `src/main/resources/templates/comptabilite/recu-charge-template.html`**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <style>
        body { font-family: Arial, sans-serif; font-size: 12px; color: #333; margin: 30px; }
        h1 { font-size: 18px; margin: 0; }
        .header { text-align: center; margin-bottom: 24px; border-bottom: 2px solid #333; padding-bottom: 12px; }
        .info-grid { display: table; width: 100%; margin-bottom: 20px; }
        .info-col { display: table-cell; width: 50%; vertical-align: top; }
        .info-col p { margin: 2px 0; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 16px; }
        th, td { border: 1px solid #ccc; padding: 6px 10px; }
        th { background-color: #f5f5f5; text-align: left; }
        .text-right { text-align: right; }
        .total-row { font-weight: bold; background-color: #e8f5e9; }
        .footer { margin-top: 40px; }
    </style>
</head>
<body>

<div class="header">
    <h1 th:text="${appName ?: 'Boulangerie'}"></h1>
    <p>RE&#199;U DE PAIEMENT</p>
</div>

<div class="info-grid">
    <div class="info-col">
        <p><strong>Type de charge :</strong> <span th:text="${charge.type}"></span></p>
        <p><strong>Description :</strong> <span th:text="${charge.description}"></span></p>
        <p><strong>P&#233;riodicit&#233; :</strong>
            <span th:text="${charge.periodicite != null ? charge.periodicite : '&#8212;'}"></span></p>
    </div>
    <div class="info-col">
        <p><strong>Date d'&#233;ch&#233;ance :</strong>
            <span th:text="${#temporals.format(charge.dateEcheance,'dd/MM/yyyy')}"></span></p>
        <p><strong>Date de paiement :</strong>
            <span th:text="${#temporals.format(charge.datePaiement,'dd/MM/yyyy')}"></span></p>
        <p th:if="${charge.transaction != null}">
            <strong>R&#233;f&#233;rence :</strong>
            <span th:text="${'#' + charge.transaction.id}"></span>
        </p>
    </div>
</div>

<table>
    <thead>
    <tr>
        <th>Libell&#233;</th>
        <th class="text-right">Montant</th>
    </tr>
    </thead>
    <tbody>
    <tr class="total-row">
        <td th:text="${charge.type + ' &#8212; ' + charge.description}"></td>
        <td class="text-right"
            th:text="${#numbers.formatDecimal(charge.montant,1,'COMMA',0,'POINT') + ' XAF'}"></td>
    </tr>
    <tr th:if="${charge.transaction != null}">
        <td>Compte d&#233;bit&#233;</td>
        <td class="text-right" th:text="${charge.transaction.compteBancaire.nom}"></td>
    </tr>
    </tbody>
</table>

<div class="footer">
    <p>Signature employeur</p>
    <br/><br/>
    <p>__________________________</p>
</div>

</body>
</html>
```

- [ ] **Step 4 : Lancer la suite complète de tests**

```
.\mvnw.cmd test 2>&1 | Select-String "Tests run|BUILD SUCCESS|BUILD FAILURE" | Select-Object -Last 5
```
Attendu : BUILD SUCCESS, tous les tests passent (79+ tests, 0 failures)

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/controller/PdfController.java
git add src/main/resources/templates/comptabilite/recu-charge-template.html
git commit -m "feat(charges): route PDF recu de paiement charge fixe"
```

---

## Vérification finale

- [ ] `GET /comptabilite/charges-fixes` → liste avec bouton "Payer" sur les non-payées, bouton PDF sur les payées
- [ ] `GET /comptabilite/charges-fixes/{id}/payer` → formulaire avec résumé charge + select compte (solde affiché)
- [ ] `POST /comptabilite/charges-fixes/{id}/payer` → Transaction créée, compte débité, `paye=true`, `datePaiement` défini, prochaine échéance créée si périodique
- [ ] `GET /comptabilite/charges-fixes/{id}/recu` → PDF téléchargé (type, description, montant, compte, dates, référence transaction)
- [ ] `GET /comptabilite/charges-fixes/{id}/recu` sur une charge non payée → erreur 400
- [ ] `mvn test` passe sans régression
