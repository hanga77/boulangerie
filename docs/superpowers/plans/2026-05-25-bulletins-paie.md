# Bulletins de Paie Employés — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Créer un module RH complet (entité Employé, bulletins de paie mensuels, PDF, intégration financière).

**Architecture:** Module autonome avec deux nouvelles entités JPA (`Employe`, `BulletinDePaie`), deux services (`EmployeService`, `BulletinDePaieService`), deux contrôleurs Thymeleaf, et une route PDF ajoutée dans `PdfController`. Le paiement crée une `Transaction` via le `Compte Principal` existant.

**Tech Stack:** Spring Boot 3.4.2, JPA/Hibernate, Thymeleaf, Bootstrap 5, Flying Saucer (ITextRenderer), JUnit 5 + Mockito

---

## File Map

| Action | Fichier |
|--------|---------|
| Create | `src/main/java/lab/hang/Gestion/boulangerie/model/StatutBulletin.java` |
| Create | `src/main/java/lab/hang/Gestion/boulangerie/model/Employe.java` |
| Create | `src/main/java/lab/hang/Gestion/boulangerie/repository/EmployeRepository.java` |
| Create | `src/main/java/lab/hang/Gestion/boulangerie/model/BulletinDePaie.java` |
| Create | `src/main/java/lab/hang/Gestion/boulangerie/repository/BulletinDePaieRepository.java` |
| Create | `src/main/java/lab/hang/Gestion/boulangerie/service/EmployeService.java` |
| Create | `src/test/java/lab/hang/gestion_boulangerie/service/EmployeServiceTest.java` |
| Create | `src/main/java/lab/hang/Gestion/boulangerie/service/BulletinDePaieService.java` |
| Create | `src/test/java/lab/hang/gestion_boulangerie/service/BulletinDePaieServiceTest.java` |
| Create | `src/main/java/lab/hang/Gestion/boulangerie/security/BulletinSecurityService.java` |
| Create | `src/main/java/lab/hang/Gestion/boulangerie/controller/EmployeController.java` |
| Create | `src/main/java/lab/hang/Gestion/boulangerie/controller/BulletinDePaieController.java` |
| Modify | `src/main/java/lab/hang/Gestion/boulangerie/controller/PdfController.java` |
| Modify | `src/main/java/lab/hang/Gestion/boulangerie/security/SecurityConfig.java` |
| Modify | `src/main/java/lab/hang/Gestion/boulangerie/config/DataInitializer.java` |
| Modify | `src/main/resources/templates/fragments/layout.html` |
| Create | `src/main/resources/templates/employes/list.html` |
| Create | `src/main/resources/templates/employes/form.html` |
| Create | `src/main/resources/templates/bulletins/list.html` |
| Create | `src/main/resources/templates/bulletins/generer.html` |
| Create | `src/main/resources/templates/bulletins/detail.html` |
| Create | `src/main/resources/templates/employes/bulletin-template.html` |

---

## Task 1 : StatutBulletin + Employe + EmployeRepository

**Files:**
- Create: `src/main/java/lab/hang/Gestion/boulangerie/model/StatutBulletin.java`
- Create: `src/main/java/lab/hang/Gestion/boulangerie/model/Employe.java`
- Create: `src/main/java/lab/hang/Gestion/boulangerie/repository/EmployeRepository.java`

- [ ] **Step 1 : Créer `StatutBulletin`**

```java
package lab.hang.Gestion.boulangerie.model;

public enum StatutBulletin {
    GENERE,
    PAYE
}
```

- [ ] **Step 2 : Créer `Employe`**

```java
package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "employe")
public class Employe {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String poste;

    private double salaireBase;
    private LocalDate dateEmbauche;
    private String numeroCnps;
    private boolean actif = true;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id")
    private User user;
}
```

- [ ] **Step 3 : Créer `EmployeRepository`**

```java
package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.Employe;
import lab.hang.Gestion.boulangerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    List<Employe> findByActifTrue();
    Optional<Employe> findByUser(User user);
}
```

- [ ] **Step 4 : Compiler**

```
mvn compile -q
```
Attendu : BUILD SUCCESS

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/model/StatutBulletin.java
git add src/main/java/lab/hang/Gestion/boulangerie/model/Employe.java
git add src/main/java/lab/hang/Gestion/boulangerie/repository/EmployeRepository.java
git commit -m "feat(rh): ajoute Employe entity, StatutBulletin enum, EmployeRepository"
```

---

## Task 2 : BulletinDePaie + BulletinDePaieRepository

**Files:**
- Create: `src/main/java/lab/hang/Gestion/boulangerie/model/BulletinDePaie.java`
- Create: `src/main/java/lab/hang/Gestion/boulangerie/repository/BulletinDePaieRepository.java`

- [ ] **Step 1 : Créer `BulletinDePaie`**

```java
package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "bulletin_de_paie",
       uniqueConstraints = @UniqueConstraint(columnNames = {"employe_id", "periode"}))
public class BulletinDePaie {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @Column(nullable = false)
    private LocalDate periode;  // toujours le 1er du mois

    private double salaireBase;
    private double primes;
    private double indemnitesTransport;
    private double salaireBrut;
    private double cnpsEmploye;     // 4,2 % du brut
    private double cnpsPatronal;    // 16,2 % du brut (informatif)
    private double avanceSurSalaire;
    private double salaireNet;      // brut - cnpsEmploye - avanceSurSalaire

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutBulletin statut;

    @Column(nullable = false)
    private LocalDate dateGeneration;

    private LocalDate datePaiement;

    @OneToOne(optional = true)
    private Transaction transaction;
}
```

- [ ] **Step 2 : Créer `BulletinDePaieRepository`**

```java
package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.BulletinDePaie;
import lab.hang.Gestion.boulangerie.model.Employe;
import lab.hang.Gestion.boulangerie.model.StatutBulletin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BulletinDePaieRepository extends JpaRepository<BulletinDePaie, Long> {
    List<BulletinDePaie> findByEmploye(Employe employe);
    List<BulletinDePaie> findByPeriodeBetween(LocalDate debut, LocalDate fin);
    List<BulletinDePaie> findByStatut(StatutBulletin statut);
    Optional<BulletinDePaie> findByEmployeAndPeriode(Employe employe, LocalDate periode);
    List<BulletinDePaie> findByEmployeAndPeriodeBetween(Employe employe, LocalDate debut, LocalDate fin);
}
```

- [ ] **Step 3 : Compiler**

```
mvn compile -q
```
Attendu : BUILD SUCCESS

- [ ] **Step 4 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/model/BulletinDePaie.java
git add src/main/java/lab/hang/Gestion/boulangerie/repository/BulletinDePaieRepository.java
git commit -m "feat(rh): ajoute BulletinDePaie entity et BulletinDePaieRepository"
```

---

## Task 3 : EmployeService + tests

**Files:**
- Create: `src/main/java/lab/hang/Gestion/boulangerie/service/EmployeService.java`
- Create: `src/test/java/lab/hang/gestion_boulangerie/service/EmployeServiceTest.java`

- [ ] **Step 1 : Écrire les tests (échouent pour l'instant)**

```java
package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.Employe;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.EmployeRepository;
import lab.hang.Gestion.boulangerie.service.EmployeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeServiceTest {

    @Mock EmployeRepository employeRepository;
    @InjectMocks EmployeService employeService;

    @Test
    void getAllActifs_returnsOnlyActifs() {
        Employe e = new Employe();
        e.setActif(true);
        when(employeRepository.findByActifTrue()).thenReturn(List.of(e));

        List<Employe> result = employeService.getAllActifs();

        assertThat(result).hasSize(1);
        verify(employeRepository).findByActifTrue();
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(employeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void desactiver_setsActifFalse() {
        Employe e = new Employe();
        e.setActif(true);
        when(employeRepository.findById(1L)).thenReturn(Optional.of(e));

        employeService.desactiver(1L);

        assertThat(e.isActif()).isFalse();
        verify(employeRepository).save(e);
    }

    @Test
    void findByUser_delegatesToRepository() {
        User user = new User();
        Employe e = new Employe();
        when(employeRepository.findByUser(user)).thenReturn(Optional.of(e));

        Optional<Employe> result = employeService.findByUser(user);

        assertThat(result).contains(e);
    }
}
```

- [ ] **Step 2 : Vérifier que les tests échouent**

```
mvn test -pl . -Dtest=EmployeServiceTest -q 2>&1 | tail -5
```
Attendu : FAILURE (classe EmployeService introuvable)

- [ ] **Step 3 : Implémenter `EmployeService`**

```java
package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.Employe;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.EmployeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EmployeService {

    private final EmployeRepository employeRepository;

    public EmployeService(EmployeRepository employeRepository) {
        this.employeRepository = employeRepository;
    }

    public List<Employe> getAllActifs() {
        return employeRepository.findByActifTrue();
    }

    public List<Employe> getAll() {
        return employeRepository.findAll();
    }

    public Employe getById(Long id) {
        return employeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable : " + id));
    }

    @Transactional
    public Employe save(Employe employe) {
        return employeRepository.save(employe);
    }

    @Transactional
    public void desactiver(Long id) {
        Employe employe = getById(id);
        employe.setActif(false);
        employeRepository.save(employe);
    }

    public Optional<Employe> findByUser(User user) {
        return employeRepository.findByUser(user);
    }
}
```

- [ ] **Step 4 : Vérifier que les tests passent**

```
mvn test -pl . -Dtest=EmployeServiceTest -q 2>&1 | tail -5
```
Attendu : BUILD SUCCESS, Tests run: 4, Failures: 0

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/service/EmployeService.java
git add src/test/java/lab/hang/gestion_boulangerie/service/EmployeServiceTest.java
git commit -m "feat(rh): ajoute EmployeService avec tests"
```

---

## Task 4 : BulletinDePaieService + tests

**Files:**
- Create: `src/main/java/lab/hang/Gestion/boulangerie/service/BulletinDePaieService.java`
- Create: `src/test/java/lab/hang/gestion_boulangerie/service/BulletinDePaieServiceTest.java`

- [ ] **Step 1 : Écrire les tests (échouent pour l'instant)**

```java
package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.*;
import lab.hang.Gestion.boulangerie.service.BulletinDePaieService;
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
class BulletinDePaieServiceTest {

    @Mock BulletinDePaieRepository bulletinRepository;
    @Mock EmployeRepository employeRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock CompteBancaireRepository compteBancaireRepository;
    @InjectMocks BulletinDePaieService bulletinService;

    private Employe employe(Long id, double salaireBase) {
        Employe e = new Employe();
        e.setId(id);
        e.setNom("Dupont");
        e.setPrenom("Jean");
        e.setSalaireBase(salaireBase);
        return e;
    }

    @Test
    void genererBulletin_calculatesCorrectAmounts() {
        Employe e = employe(1L, 80_000.0);
        LocalDate periode = LocalDate.of(2026, 5, 1);

        when(employeRepository.findById(1L)).thenReturn(Optional.of(e));
        when(bulletinRepository.findByEmployeAndPeriode(e, periode)).thenReturn(Optional.empty());
        when(bulletinRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BulletinDePaie b = bulletinService.genererBulletin(1L, periode, 10_000.0, 5_000.0, 0.0);

        assertThat(b.getSalaireBrut()).isEqualTo(95_000.0);
        assertThat(b.getCnpsEmploye()).isCloseTo(3_990.0, within(0.01));   // 95000 * 4.2%
        assertThat(b.getCnpsPatronal()).isCloseTo(15_390.0, within(0.01)); // 95000 * 16.2%
        assertThat(b.getSalaireNet()).isCloseTo(91_010.0, within(0.01));   // 95000 - 3990
        assertThat(b.getStatut()).isEqualTo(StatutBulletin.GENERE);
    }

    @Test
    void genererBulletin_throwsWhenDuplicate() {
        Employe e = employe(1L, 80_000.0);
        LocalDate periode = LocalDate.of(2026, 5, 1);

        when(employeRepository.findById(1L)).thenReturn(Optional.of(e));
        when(bulletinRepository.findByEmployeAndPeriode(e, periode))
            .thenReturn(Optional.of(new BulletinDePaie()));

        assertThatThrownBy(() -> bulletinService.genererBulletin(1L, periode, 0, 0, 0))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("bulletin existe déjà");
    }

    @Test
    void payerBulletin_createsTransactionAndUpdatesStatut() {
        Employe e = employe(1L, 80_000.0);
        BulletinDePaie bulletin = new BulletinDePaie();
        bulletin.setId(10L);
        bulletin.setEmploye(e);
        bulletin.setPeriode(LocalDate.of(2026, 5, 1));
        bulletin.setSalaireNet(91_010.0);
        bulletin.setStatut(StatutBulletin.GENERE);

        CompteBancaire compte = new CompteBancaire();
        compte.setSolde(500_000.0);

        when(bulletinRepository.findById(10L)).thenReturn(Optional.of(bulletin));
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(Optional.of(compte));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bulletinRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BulletinDePaie result = bulletinService.payerBulletin(10L);

        assertThat(result.getStatut()).isEqualTo(StatutBulletin.PAYE);
        assertThat(result.getDatePaiement()).isNotNull();
        assertThat(result.getTransaction()).isNotNull();
        assertThat(compte.getSolde()).isCloseTo(500_000.0 - 91_010.0, within(0.01));

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getType()).isEqualTo("SALAIRE");
        assertThat(txCaptor.getValue().getMontant()).isCloseTo(91_010.0, within(0.01));
    }

    @Test
    void payerBulletin_throwsWhenAlreadyPaid() {
        BulletinDePaie bulletin = new BulletinDePaie();
        bulletin.setStatut(StatutBulletin.PAYE);
        when(bulletinRepository.findById(5L)).thenReturn(Optional.of(bulletin));

        assertThatThrownBy(() -> bulletinService.payerBulletin(5L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("déjà été payé");
    }
}
```

- [ ] **Step 2 : Vérifier que les tests échouent**

```
mvn test -pl . -Dtest=BulletinDePaieServiceTest -q 2>&1 | tail -5
```
Attendu : FAILURE

- [ ] **Step 3 : Implémenter `BulletinDePaieService`**

```java
package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BulletinDePaieService {

    private static final double TAUX_CNPS_EMPLOYE  = 0.042;
    private static final double TAUX_CNPS_PATRONAL = 0.162;

    private final BulletinDePaieRepository bulletinRepository;
    private final EmployeRepository employeRepository;
    private final TransactionRepository transactionRepository;
    private final CompteBancaireRepository compteBancaireRepository;

    public BulletinDePaieService(BulletinDePaieRepository bulletinRepository,
                                  EmployeRepository employeRepository,
                                  TransactionRepository transactionRepository,
                                  CompteBancaireRepository compteBancaireRepository) {
        this.bulletinRepository = bulletinRepository;
        this.employeRepository  = employeRepository;
        this.transactionRepository = transactionRepository;
        this.compteBancaireRepository = compteBancaireRepository;
    }

    @Transactional
    public BulletinDePaie genererBulletin(Long employeId, LocalDate periode,
                                          double primes, double indemnitesTransport,
                                          double avanceSurSalaire) {
        Employe employe = employeRepository.findById(employeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable : " + employeId));

        if (bulletinRepository.findByEmployeAndPeriode(employe, periode).isPresent()) {
            throw new IllegalStateException(
                "Un bulletin existe déjà pour " + employe.getNom() + " sur la période " + periode);
        }

        double salaireBase  = employe.getSalaireBase();
        double salaireBrut  = salaireBase + primes + indemnitesTransport;
        double cnpsEmploye  = salaireBrut * TAUX_CNPS_EMPLOYE;
        double cnpsPatronal = salaireBrut * TAUX_CNPS_PATRONAL;
        double salaireNet   = salaireBrut - cnpsEmploye - avanceSurSalaire;

        BulletinDePaie bulletin = new BulletinDePaie();
        bulletin.setEmploye(employe);
        bulletin.setPeriode(periode);
        bulletin.setSalaireBase(salaireBase);
        bulletin.setPrimes(primes);
        bulletin.setIndemnitesTransport(indemnitesTransport);
        bulletin.setSalaireBrut(salaireBrut);
        bulletin.setCnpsEmploye(cnpsEmploye);
        bulletin.setCnpsPatronal(cnpsPatronal);
        bulletin.setAvanceSurSalaire(avanceSurSalaire);
        bulletin.setSalaireNet(salaireNet);
        bulletin.setStatut(StatutBulletin.GENERE);
        bulletin.setDateGeneration(LocalDate.now());

        return bulletinRepository.save(bulletin);
    }

    @Transactional
    public BulletinDePaie payerBulletin(Long bulletinId) {
        BulletinDePaie bulletin = bulletinRepository.findById(bulletinId)
            .orElseThrow(() -> new ResourceNotFoundException("Bulletin introuvable : " + bulletinId));

        if (bulletin.getStatut() == StatutBulletin.PAYE) {
            throw new IllegalStateException("Ce bulletin a déjà été payé.");
        }

        CompteBancaire compte = compteBancaireRepository.findByNom("Compte Principal")
            .orElseThrow(() -> new ResourceNotFoundException("Compte Principal introuvable"));

        Employe employe = bulletin.getEmploye();
        String moisAnnee = bulletin.getPeriode()
            .getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH)
            + " " + bulletin.getPeriode().getYear();

        Transaction transaction = new Transaction();
        transaction.setType("SALAIRE");
        transaction.setMontant(bulletin.getSalaireNet());
        transaction.setDate(LocalDate.now());
        transaction.setDescription("Salaire " + employe.getPrenom() + " " + employe.getNom()
            + " — " + moisAnnee);
        transaction.setCompteBancaire(compte);

        compte.setSolde(compte.getSolde() - bulletin.getSalaireNet());
        compteBancaireRepository.save(compte);
        Transaction savedTx = transactionRepository.save(transaction);

        bulletin.setStatut(StatutBulletin.PAYE);
        bulletin.setDatePaiement(LocalDate.now());
        bulletin.setTransaction(savedTx);

        return bulletinRepository.save(bulletin);
    }

    public BulletinDePaie getById(Long id) {
        return bulletinRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bulletin introuvable : " + id));
    }

    public List<BulletinDePaie> getBulletinsParEmploye(Employe employe) {
        return bulletinRepository.findByEmploye(employe);
    }

    public List<BulletinDePaie> getBulletinsFiltres(LocalDate debut, LocalDate fin, StatutBulletin statut) {
        List<BulletinDePaie> bulletins = bulletinRepository.findByPeriodeBetween(debut, fin);
        if (statut != null) {
            return bulletins.stream().filter(b -> b.getStatut() == statut).collect(Collectors.toList());
        }
        return bulletins;
    }

    public List<BulletinDePaie> getByStatut(StatutBulletin statut) {
        return bulletinRepository.findByStatut(statut);
    }

    public double sumNetByStatut(StatutBulletin statut, LocalDate debut, LocalDate fin) {
        return bulletinRepository.findByPeriodeBetween(debut, fin).stream()
            .filter(b -> b.getStatut() == statut)
            .mapToDouble(BulletinDePaie::getSalaireNet)
            .sum();
    }
}
```

- [ ] **Step 4 : Vérifier que les tests passent**

```
mvn test -pl . -Dtest=BulletinDePaieServiceTest -q 2>&1 | tail -5
```
Attendu : BUILD SUCCESS, Tests run: 4, Failures: 0

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/service/BulletinDePaieService.java
git add src/test/java/lab/hang/gestion_boulangerie/service/BulletinDePaieServiceTest.java
git commit -m "feat(rh): ajoute BulletinDePaieService avec tests (generer + payer)"
```

---

## Task 5 : BulletinSecurityService + EmployeController + templates employes/

**Files:**
- Create: `src/main/java/lab/hang/Gestion/boulangerie/security/BulletinSecurityService.java`
- Create: `src/main/java/lab/hang/Gestion/boulangerie/controller/EmployeController.java`
- Create: `src/main/resources/templates/employes/list.html`
- Create: `src/main/resources/templates/employes/form.html`

- [ ] **Step 1 : Créer `BulletinSecurityService`**

Ce composant est utilisé dans `@PreAuthorize` pour vérifier que le BOULANGER courant est l'employé lié au bulletin.

```java
package lab.hang.Gestion.boulangerie.security;

import lab.hang.Gestion.boulangerie.repository.BulletinDePaieRepository;
import lab.hang.Gestion.boulangerie.repository.EmployeRepository;
import lab.hang.Gestion.boulangerie.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("bulletinSecurity")
public class BulletinSecurityService {

    private final BulletinDePaieRepository bulletinRepository;
    private final EmployeRepository employeRepository;
    private final UserRepository userRepository;

    public BulletinSecurityService(BulletinDePaieRepository bulletinRepository,
                                   EmployeRepository employeRepository,
                                   UserRepository userRepository) {
        this.bulletinRepository = bulletinRepository;
        this.employeRepository  = employeRepository;
        this.userRepository     = userRepository;
    }

    public boolean isBulletinOwner(Long bulletinId, Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
            .flatMap(employeRepository::findByUser)
            .map(employe -> bulletinRepository.findById(bulletinId)
                .map(b -> b.getEmploye().getId().equals(employe.getId()))
                .orElse(false))
            .orElse(false);
    }
}
```

- [ ] **Step 2 : Créer `EmployeController`**

```java
package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.model.Employe;
import lab.hang.Gestion.boulangerie.service.EmployeService;
import lab.hang.Gestion.boulangerie.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employes")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class EmployeController {

    private final EmployeService employeService;
    private final UserService userService;

    public EmployeController(EmployeService employeService, UserService userService) {
        this.employeService = employeService;
        this.userService    = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employes", employeService.getAllActifs());
        return "employes/list";
    }

    @GetMapping("/nouveau")
    public String showCreateForm(Model model) {
        model.addAttribute("employe", new Employe());
        model.addAttribute("users", userService.getAllUsers());
        return "employes/form";
    }

    @PostMapping("/nouveau")
    public String create(@ModelAttribute Employe employe,
                         @RequestParam(required = false) Long userId,
                         RedirectAttributes ra) {
        if (userId != null) {
            employe.setUser(userService.getUserById(userId));
        }
        employeService.save(employe);
        ra.addFlashAttribute("successMessage", "Employé créé avec succès.");
        return "redirect:/employes";
    }

    @GetMapping("/{id}/modifier")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("employe", employeService.getById(id));
        model.addAttribute("users", userService.getAllUsers());
        return "employes/form";
    }

    @PostMapping("/{id}/modifier")
    public String update(@PathVariable Long id,
                         @ModelAttribute Employe employe,
                         @RequestParam(required = false) Long userId,
                         RedirectAttributes ra) {
        Employe existing = employeService.getById(id);
        existing.setNom(employe.getNom());
        existing.setPrenom(employe.getPrenom());
        existing.setPoste(employe.getPoste());
        existing.setSalaireBase(employe.getSalaireBase());
        existing.setDateEmbauche(employe.getDateEmbauche());
        existing.setNumeroCnps(employe.getNumeroCnps());
        existing.setUser(userId != null ? userService.getUserById(userId) : null);
        employeService.save(existing);
        ra.addFlashAttribute("successMessage", "Employé mis à jour.");
        return "redirect:/employes";
    }

    @PostMapping("/{id}/desactiver")
    public String desactiver(@PathVariable Long id, RedirectAttributes ra) {
        employeService.desactiver(id);
        ra.addFlashAttribute("successMessage", "Employé désactivé.");
        return "redirect:/employes";
    }
}
```

- [ ] **Step 3 : Créer `src/main/resources/templates/employes/list.html`**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/layout :: head('Employés')}"></head>
<body>
<nav th:replace="~{fragments/layout :: navbar}"></nav>
<div th:replace="~{fragments/layout :: flash-messages}"></div>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="h4 fw-bold mb-0"><i class="fas fa-users me-2 text-primary"></i>Employés</h1>
            <p class="text-muted small mb-0">Registre des employés actifs</p>
        </div>
        <a href="/employes/nouveau" class="btn btn-primary btn-sm">
            <i class="fas fa-user-plus me-1"></i>Nouvel employé
        </a>
    </div>

    <div class="card shadow-sm">
        <div class="table-responsive">
            <table class="table table-hover mb-0">
                <thead class="table-light">
                <tr>
                    <th>Nom</th>
                    <th>Prénom</th>
                    <th>Poste</th>
                    <th class="text-end">Salaire base</th>
                    <th>Compte lié</th>
                    <th class="text-end">Actions</th>
                </tr>
                </thead>
                <tbody>
                <tr th:each="e : ${employes}">
                    <td class="fw-semibold" th:text="${e.nom}"></td>
                    <td th:text="${e.prenom}"></td>
                    <td><span class="badge bg-secondary" th:text="${e.poste}"></span></td>
                    <td class="text-end" th:text="${#numbers.formatDecimal(e.salaireBase,1,'COMMA',0,'POINT') + ' XAF'}"></td>
                    <td>
                        <span th:if="${e.user != null}" class="badge bg-info text-dark" th:text="${e.user.username}"></span>
                        <span th:unless="${e.user != null}" class="text-muted small">—</span>
                    </td>
                    <td class="text-end">
                        <div class="d-flex justify-content-end gap-1 flex-wrap">
                            <a th:href="@{/employes/{id}/modifier(id=${e.id})}"
                               class="btn btn-outline-primary btn-sm" title="Modifier">
                                <i class="fas fa-edit"></i>
                            </a>
                            <a th:href="@{/bulletins/generer(employeId=${e.id})}"
                               class="btn btn-outline-success btn-sm" title="Nouveau bulletin">
                                <i class="fas fa-file-invoice-dollar"></i>
                            </a>
                            <form th:action="@{/employes/{id}/desactiver(id=${e.id})}" method="post"
                                  onsubmit="return confirm('Désactiver cet employé ?')">
                                <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
                                <button type="submit" class="btn btn-outline-danger btn-sm" title="Désactiver">
                                    <i class="fas fa-user-slash"></i>
                                </button>
                            </form>
                        </div>
                    </td>
                </tr>
                <tr th:if="${#lists.isEmpty(employes)}">
                    <td colspan="6" class="text-center text-muted py-4">Aucun employé actif.</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>
<div th:replace="~{fragments/layout :: scripts}"></div>
</body>
</html>
```

- [ ] **Step 4 : Créer `src/main/resources/templates/employes/form.html`**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/layout :: head('Employé')}"></head>
<body>
<nav th:replace="~{fragments/layout :: navbar}"></nav>
<div th:replace="~{fragments/layout :: flash-messages}"></div>

<div class="container mt-4" style="max-width:640px">
    <div class="mb-4">
        <h1 class="h4 fw-bold mb-0">
            <i class="fas fa-user-edit me-2 text-primary"></i>
            <span th:text="${employe.id == null ? 'Nouvel employé' : 'Modifier l\'employé'}"></span>
        </h1>
    </div>

    <div class="card shadow-sm">
        <div class="card-body">
            <form th:action="${employe.id == null ? '/employes/nouveau' : '/employes/' + employe.id + '/modifier'}"
                  method="post">
                <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>

                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Nom <span class="text-danger">*</span></label>
                        <input type="text" name="nom" class="form-control"
                               th:value="${employe.nom}" required/>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Prénom <span class="text-danger">*</span></label>
                        <input type="text" name="prenom" class="form-control"
                               th:value="${employe.prenom}" required/>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Poste <span class="text-danger">*</span></label>
                        <input type="text" name="poste" class="form-control"
                               th:value="${employe.poste}" placeholder="ex. Boulanger" required/>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Salaire de base (XAF) <span class="text-danger">*</span></label>
                        <input type="number" name="salaireBase" class="form-control"
                               th:value="${employe.salaireBase}" min="0" step="500" required/>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Date d'embauche</label>
                        <input type="date" name="dateEmbauche" class="form-control"
                               th:value="${employe.dateEmbauche}"/>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">N° CNPS</label>
                        <input type="text" name="numeroCnps" class="form-control"
                               th:value="${employe.numeroCnps}" placeholder="Optionnel"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label fw-semibold">Compte utilisateur lié</label>
                        <select name="userId" class="form-select">
                            <option value="">— Aucun —</option>
                            <option th:each="u : ${users}"
                                    th:value="${u.id}"
                                    th:text="${u.username + ' (' + u.role + ')'}"
                                    th:selected="${employe.user != null && employe.user.id == u.id}">
                            </option>
                        </select>
                        <div class="form-text">Permet à cet utilisateur d'accéder à ses bulletins.</div>
                    </div>
                </div>

                <div class="d-flex gap-2 mt-4">
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-save me-1"></i>Enregistrer
                    </button>
                    <a href="/employes" class="btn btn-outline-secondary">Annuler</a>
                </div>
            </form>
        </div>
    </div>
</div>
<div th:replace="~{fragments/layout :: scripts}"></div>
</body>
</html>
```

- [ ] **Step 5 : Compiler**

```
mvn compile -q
```
Attendu : BUILD SUCCESS

- [ ] **Step 6 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/security/BulletinSecurityService.java
git add src/main/java/lab/hang/Gestion/boulangerie/controller/EmployeController.java
git add src/main/resources/templates/employes/list.html
git add src/main/resources/templates/employes/form.html
git commit -m "feat(rh): ajoute EmployeController et templates employes"
```

---

## Task 6 : BulletinDePaieController + templates bulletins/

**Files:**
- Create: `src/main/java/lab/hang/Gestion/boulangerie/controller/BulletinDePaieController.java`
- Create: `src/main/resources/templates/bulletins/list.html`
- Create: `src/main/resources/templates/bulletins/generer.html`
- Create: `src/main/resources/templates/bulletins/detail.html`

- [ ] **Step 1 : Créer `BulletinDePaieController`**

```java
package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/bulletins")
public class BulletinDePaieController {

    private final BulletinDePaieService bulletinService;
    private final EmployeService employeService;
    private final UserService userService;

    public BulletinDePaieController(BulletinDePaieService bulletinService,
                                    EmployeService employeService,
                                    UserService userService) {
        this.bulletinService = bulletinService;
        this.employeService  = employeService;
        this.userService     = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String list(@RequestParam(required = false) Integer mois,
                       @RequestParam(required = false) Integer annee,
                       @RequestParam(required = false) StatutBulletin statut,
                       @RequestParam(required = false) Long employeId,
                       Model model) {
        int m = mois  != null ? mois  : LocalDate.now().getMonthValue();
        int a = annee != null ? annee : LocalDate.now().getYear();
        LocalDate debut = LocalDate.of(a, m, 1);
        LocalDate fin   = debut.withDayOfMonth(debut.lengthOfMonth());

        List<BulletinDePaie> bulletins;
        if (employeId != null) {
            Employe emp = employeService.getById(employeId);
            bulletins = bulletinService.getBulletinsParEmploye(emp).stream()
                .filter(b -> !b.getPeriode().isBefore(debut) && !b.getPeriode().isAfter(fin))
                .filter(b -> statut == null || b.getStatut() == statut)
                .toList();
        } else {
            bulletins = bulletinService.getBulletinsFiltres(debut, fin, statut);
        }

        long nbGenere   = bulletins.stream().filter(b -> b.getStatut() == StatutBulletin.GENERE).count();
        double totalNet = bulletins.stream().filter(b -> b.getStatut() == StatutBulletin.GENERE)
                            .mapToDouble(BulletinDePaie::getSalaireNet).sum();
        double totalPaye = bulletins.stream().filter(b -> b.getStatut() == StatutBulletin.PAYE)
                            .mapToDouble(BulletinDePaie::getSalaireNet).sum();

        model.addAttribute("bulletins", bulletins);
        model.addAttribute("employes", employeService.getAllActifs());
        model.addAttribute("moisActuel", m);
        model.addAttribute("anneeActuelle", a);
        model.addAttribute("statutFiltre", statut);
        model.addAttribute("employeIdFiltre", employeId);
        model.addAttribute("nbGenere", nbGenere);
        model.addAttribute("totalNet", totalNet);
        model.addAttribute("totalPaye", totalPaye);
        return "bulletins/list";
    }

    @GetMapping("/generer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String showGenererForm(@RequestParam(required = false) Long employeId, Model model) {
        model.addAttribute("employes", employeService.getAllActifs());
        model.addAttribute("employeIdPrefill", employeId);
        model.addAttribute("moisActuel", LocalDate.now().getMonthValue());
        model.addAttribute("anneeActuelle", LocalDate.now().getYear());
        return "bulletins/generer";
    }

    @PostMapping("/generer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String generer(@RequestParam Long employeId,
                          @RequestParam int mois,
                          @RequestParam int annee,
                          @RequestParam(defaultValue = "0") double primes,
                          @RequestParam(defaultValue = "0") double indemnitesTransport,
                          @RequestParam(defaultValue = "0") double avanceSurSalaire,
                          RedirectAttributes ra) {
        LocalDate periode = LocalDate.of(annee, mois, 1);
        try {
            BulletinDePaie bulletin = bulletinService.genererBulletin(
                employeId, periode, primes, indemnitesTransport, avanceSurSalaire);
            ra.addFlashAttribute("successMessage",
                "Bulletin généré pour " + bulletin.getEmploye().getNom() + ".");
            return "redirect:/bulletins/" + bulletin.getId();
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bulletins/generer";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or @bulletinSecurity.isBulletinOwner(#id, authentication)")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("bulletin", bulletinService.getById(id));
        return "bulletins/detail";
    }

    @PostMapping("/{id}/payer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String payer(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bulletinService.payerBulletin(id);
            ra.addFlashAttribute("successMessage", "Paiement effectué avec succès.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/bulletins/" + id;
    }

    @GetMapping("/mes-bulletins")
    public String mesBulletins(Model model) {
        User currentUser = userService.getCurrentUser();
        employeService.findByUser(currentUser).ifPresentOrElse(
            employe -> {
                model.addAttribute("employe", employe);
                model.addAttribute("bulletins", bulletinService.getBulletinsParEmploye(employe));
            },
            () -> model.addAttribute("aucunProfil", true)
        );
        return "bulletins/mes-bulletins";
    }
}
```

- [ ] **Step 2 : Créer `src/main/resources/templates/bulletins/list.html`**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head th:replace="~{fragments/layout :: head('Bulletins de paie')}"></head>
<body>
<nav th:replace="~{fragments/layout :: navbar}"></nav>
<div th:replace="~{fragments/layout :: flash-messages}"></div>

<div class="container-fluid px-3 px-md-4 mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="h4 fw-bold mb-0"><i class="fas fa-file-invoice-dollar me-2 text-primary"></i>Bulletins de paie</h1>
        </div>
        <a href="/bulletins/generer" class="btn btn-primary btn-sm">
            <i class="fas fa-plus me-1"></i>Nouveau bulletin
        </a>
    </div>

    <!-- KPIs -->
    <div class="row g-3 mb-4">
        <div class="col-md-4">
            <div class="card border-0 shadow-sm">
                <div class="card-body d-flex align-items-center gap-3">
                    <div class="rounded-circle bg-warning bg-opacity-10 p-3">
                        <i class="fas fa-clock fa-lg text-warning"></i>
                    </div>
                    <div>
                        <div class="text-muted small">En attente</div>
                        <div class="fw-bold fs-5" th:text="${nbGenere}">0</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card border-0 shadow-sm">
                <div class="card-body d-flex align-items-center gap-3">
                    <div class="rounded-circle bg-danger bg-opacity-10 p-3">
                        <i class="fas fa-money-bill fa-lg text-danger"></i>
                    </div>
                    <div>
                        <div class="text-muted small">Net à payer (non payés)</div>
                        <div class="fw-bold fs-5" th:text="${#numbers.formatDecimal(totalNet,1,'COMMA',0,'POINT') + ' XAF'}">0</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card border-0 shadow-sm">
                <div class="card-body d-flex align-items-center gap-3">
                    <div class="rounded-circle bg-success bg-opacity-10 p-3">
                        <i class="fas fa-check-circle fa-lg text-success"></i>
                    </div>
                    <div>
                        <div class="text-muted small">Payé ce mois</div>
                        <div class="fw-bold fs-5" th:text="${#numbers.formatDecimal(totalPaye,1,'COMMA',0,'POINT') + ' XAF'}">0</div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Filtres -->
    <form method="get" action="/bulletins" class="card shadow-sm mb-4">
        <div class="card-body">
            <div class="row g-2 align-items-end">
                <div class="col-sm-2">
                    <label class="form-label small fw-semibold">Mois</label>
                    <select name="mois" class="form-select form-select-sm">
                        <option th:each="i : ${#numbers.sequence(1,12)}"
                                th:value="${i}"
                                th:text="${#temporals.format(#temporals.create(2000,i,1),'MMMM')}"
                                th:selected="${i == moisActuel}">
                        </option>
                    </select>
                </div>
                <div class="col-sm-2">
                    <label class="form-label small fw-semibold">Année</label>
                    <input type="number" name="annee" class="form-control form-control-sm"
                           th:value="${anneeActuelle}" min="2020" max="2030"/>
                </div>
                <div class="col-sm-3">
                    <label class="form-label small fw-semibold">Employé</label>
                    <select name="employeId" class="form-select form-select-sm">
                        <option value="">Tous</option>
                        <option th:each="e : ${employes}"
                                th:value="${e.id}"
                                th:text="${e.prenom + ' ' + e.nom}"
                                th:selected="${e.id == employeIdFiltre}">
                        </option>
                    </select>
                </div>
                <div class="col-sm-2">
                    <label class="form-label small fw-semibold">Statut</label>
                    <select name="statut" class="form-select form-select-sm">
                        <option value="">Tous</option>
                        <option value="GENERE" th:selected="${statutFiltre != null && statutFiltre.name() == 'GENERE'}">En attente</option>
                        <option value="PAYE"   th:selected="${statutFiltre != null && statutFiltre.name() == 'PAYE'}">Payé</option>
                    </select>
                </div>
                <div class="col-sm-auto">
                    <button type="submit" class="btn btn-primary btn-sm">
                        <i class="fas fa-search me-1"></i>Filtrer
                    </button>
                </div>
            </div>
        </div>
    </form>

    <!-- Tableau -->
    <div class="card shadow-sm">
        <div class="table-responsive">
            <table class="table table-hover mb-0">
                <thead class="table-light">
                <tr>
                    <th>Employé</th>
                    <th>Période</th>
                    <th class="text-end">Brut</th>
                    <th class="text-end">Net</th>
                    <th>Statut</th>
                    <th class="text-end">Actions</th>
                </tr>
                </thead>
                <tbody>
                <tr th:each="b : ${bulletins}">
                    <td th:text="${b.employe.prenom + ' ' + b.employe.nom}"></td>
                    <td th:text="${#temporals.format(b.periode,'MMMM yyyy')}"></td>
                    <td class="text-end" th:text="${#numbers.formatDecimal(b.salaireBrut,1,'COMMA',0,'POINT') + ' XAF'}"></td>
                    <td class="text-end fw-semibold" th:text="${#numbers.formatDecimal(b.salaireNet,1,'COMMA',0,'POINT') + ' XAF'}"></td>
                    <td>
                        <span th:if="${b.statut.name() == 'GENERE'}" class="badge bg-warning text-dark">En attente</span>
                        <span th:if="${b.statut.name() == 'PAYE'}"   class="badge bg-success">Payé</span>
                    </td>
                    <td class="text-end">
                        <a th:href="@{/bulletins/{id}(id=${b.id})}" class="btn btn-outline-primary btn-sm">
                            <i class="fas fa-eye"></i>
                        </a>
                    </td>
                </tr>
                <tr th:if="${#lists.isEmpty(bulletins)}">
                    <td colspan="6" class="text-center text-muted py-4">Aucun bulletin pour cette période.</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>
<div th:replace="~{fragments/layout :: scripts}"></div>
</body>
</html>
```

- [ ] **Step 3 : Créer `src/main/resources/templates/bulletins/generer.html`**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/layout :: head('Générer un bulletin')}"></head>
<body>
<nav th:replace="~{fragments/layout :: navbar}"></nav>
<div th:replace="~{fragments/layout :: flash-messages}"></div>

<div class="container mt-4" style="max-width:640px">
    <div class="mb-4">
        <h1 class="h4 fw-bold mb-0"><i class="fas fa-file-invoice-dollar me-2 text-primary"></i>Générer un bulletin de paie</h1>
    </div>

    <div class="card shadow-sm">
        <div class="card-body">
            <form action="/bulletins/generer" method="post" id="genererForm">
                <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>

                <div class="row g-3">
                    <div class="col-12">
                        <label class="form-label fw-semibold">Employé <span class="text-danger">*</span></label>
                        <select name="employeId" id="employeSelect" class="form-select" required
                                onchange="updateSalaireBase(this)">
                            <option value="">— Sélectionner —</option>
                            <option th:each="e : ${employes}"
                                    th:value="${e.id}"
                                    th:text="${e.prenom + ' ' + e.nom + ' — ' + e.poste}"
                                    th:data-salaire="${e.salaireBase}"
                                    th:selected="${e.id == employeIdPrefill}">
                            </option>
                        </select>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Mois <span class="text-danger">*</span></label>
                        <select name="mois" class="form-select" required onchange="recalcul()">
                            <option th:each="i : ${#numbers.sequence(1,12)}"
                                    th:value="${i}"
                                    th:text="${#temporals.format(#temporals.create(2000,i,1),'MMMM')}"
                                    th:selected="${i == moisActuel}">
                            </option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Année <span class="text-danger">*</span></label>
                        <input type="number" name="annee" class="form-control"
                               th:value="${anneeActuelle}" min="2020" max="2030" required onchange="recalcul()"/>
                    </div>

                    <div class="col-12"><hr class="my-1"/><p class="text-muted small mb-0">Composantes variables</p></div>

                    <div class="col-md-4">
                        <label class="form-label fw-semibold">Primes (XAF)</label>
                        <input type="number" name="primes" id="primes" class="form-control"
                               value="0" min="0" step="500" onchange="recalcul()"/>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label fw-semibold">Indemnité transport (XAF)</label>
                        <input type="number" name="indemnitesTransport" id="indemnitesTransport"
                               class="form-control" value="0" min="0" step="500" onchange="recalcul()"/>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label fw-semibold">Avance sur salaire (XAF)</label>
                        <input type="number" name="avanceSurSalaire" id="avanceSurSalaire"
                               class="form-control" value="0" min="0" step="500" onchange="recalcul()"/>
                    </div>
                </div>

                <!-- Résumé calculé en JS -->
                <div id="resume" class="mt-4 d-none">
                    <div class="card bg-light border-0">
                        <div class="card-body">
                            <h6 class="fw-semibold mb-3">Aperçu du bulletin</h6>
                            <table class="table table-sm mb-0">
                                <tr><td>Salaire de base</td><td class="text-end" id="r-base">—</td></tr>
                                <tr><td>Primes</td><td class="text-end" id="r-primes">—</td></tr>
                                <tr><td>Indemnité transport</td><td class="text-end" id="r-transport">—</td></tr>
                                <tr class="fw-semibold"><td>Salaire brut</td><td class="text-end" id="r-brut">—</td></tr>
                                <tr class="text-danger"><td>CNPS employé (4,2%)</td><td class="text-end" id="r-cnps">—</td></tr>
                                <tr class="text-danger"><td>Avance sur salaire</td><td class="text-end" id="r-avance">—</td></tr>
                                <tr class="fw-bold table-success"><td>Net à payer</td><td class="text-end" id="r-net">—</td></tr>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="d-flex gap-2 mt-4">
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-save me-1"></i>Générer le bulletin
                    </button>
                    <a href="/bulletins" class="btn btn-outline-secondary">Annuler</a>
                </div>
            </form>
        </div>
    </div>
</div>

<div th:replace="~{fragments/layout :: scripts}"></div>
<script>
    let salaireBase = 0;

    function updateSalaireBase(sel) {
        const opt = sel.options[sel.selectedIndex];
        salaireBase = parseFloat(opt.getAttribute('data-salaire') || '0');
        recalcul();
    }

    function recalcul() {
        if (salaireBase <= 0) return;
        const primes = parseFloat(document.getElementById('primes').value) || 0;
        const transport = parseFloat(document.getElementById('indemnitesTransport').value) || 0;
        const avance = parseFloat(document.getElementById('avanceSurSalaire').value) || 0;

        const brut = salaireBase + primes + transport;
        const cnps = brut * 0.042;
        const net  = brut - cnps - avance;

        const fmt = v => v.toLocaleString('fr-FR') + ' XAF';
        document.getElementById('r-base').textContent     = fmt(salaireBase);
        document.getElementById('r-primes').textContent   = fmt(primes);
        document.getElementById('r-transport').textContent= fmt(transport);
        document.getElementById('r-brut').textContent     = fmt(brut);
        document.getElementById('r-cnps').textContent     = '− ' + fmt(cnps);
        document.getElementById('r-avance').textContent   = '− ' + fmt(avance);
        document.getElementById('r-net').textContent      = fmt(net);
        document.getElementById('resume').classList.remove('d-none');
    }

    // Init si un employé est pré-sélectionné
    window.addEventListener('DOMContentLoaded', () => {
        const sel = document.getElementById('employeSelect');
        if (sel.value) updateSalaireBase(sel);
    });
</script>
</body>
</html>
```

- [ ] **Step 4 : Créer `src/main/resources/templates/bulletins/detail.html`**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head th:replace="~{fragments/layout :: head('Bulletin de paie')}"></head>
<body>
<nav th:replace="~{fragments/layout :: navbar}"></nav>
<div th:replace="~{fragments/layout :: flash-messages}"></div>

<div class="container mt-4" style="max-width:680px">
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h1 class="h4 fw-bold mb-0">
                <i class="fas fa-file-invoice-dollar me-2 text-primary"></i>
                Bulletin de paie —
                <span th:text="${#temporals.format(bulletin.periode,'MMMM yyyy')}"></span>
            </h1>
            <p class="text-muted small mb-0" th:text="${bulletin.employe.prenom + ' ' + bulletin.employe.nom + ' — ' + bulletin.employe.poste}"></p>
        </div>
        <div class="d-flex gap-2 flex-wrap">
            <span th:if="${bulletin.statut.name() == 'GENERE'}" class="badge bg-warning text-dark fs-6">En attente</span>
            <span th:if="${bulletin.statut.name() == 'PAYE'}"   class="badge bg-success fs-6">Payé</span>
            <a th:href="@{/bulletins/{id}/pdf(id=${bulletin.id})}"
               class="btn btn-outline-secondary btn-sm">
                <i class="fas fa-file-pdf me-1"></i>PDF
            </a>
            <form th:if="${bulletin.statut.name() == 'GENERE'}"
                  sec:authorize="hasAnyRole('ADMIN','MANAGER')"
                  th:action="@{/bulletins/{id}/payer(id=${bulletin.id})}" method="post"
                  onsubmit="return confirm('Confirmer le paiement de ' + /*[[${#numbers.formatDecimal(bulletin.salaireNet,1,'COMMA',0,'POINT')}]]*/ '?' )">
                <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
                <button type="submit" class="btn btn-success btn-sm">
                    <i class="fas fa-check me-1"></i>Payer
                </button>
            </form>
        </div>
    </div>

    <div class="card shadow-sm">
        <div class="card-body">

            <!-- En-tête employé -->
            <div class="row mb-4">
                <div class="col-md-6">
                    <p class="mb-1 small text-muted">Employé</p>
                    <p class="fw-semibold mb-0" th:text="${bulletin.employe.prenom + ' ' + bulletin.employe.nom}"></p>
                    <p class="small text-muted mb-0" th:text="${bulletin.employe.poste}"></p>
                </div>
                <div class="col-md-3">
                    <p class="mb-1 small text-muted">N° CNPS</p>
                    <p class="mb-0" th:text="${bulletin.employe.numeroCnps ?: '—'}"></p>
                </div>
                <div class="col-md-3">
                    <p class="mb-1 small text-muted">Période</p>
                    <p class="mb-0" th:text="${#temporals.format(bulletin.periode,'MMMM yyyy')}"></p>
                </div>
            </div>

            <!-- Tableau des composantes -->
            <table class="table table-bordered mb-0">
                <tbody>
                <tr>
                    <td>Salaire de base</td>
                    <td class="text-end" th:text="${#numbers.formatDecimal(bulletin.salaireBase,1,'COMMA',0,'POINT') + ' XAF'}"></td>
                </tr>
                <tr>
                    <td>Primes</td>
                    <td class="text-end" th:text="${#numbers.formatDecimal(bulletin.primes,1,'COMMA',0,'POINT') + ' XAF'}"></td>
                </tr>
                <tr>
                    <td>Indemnité de transport</td>
                    <td class="text-end" th:text="${#numbers.formatDecimal(bulletin.indemnitesTransport,1,'COMMA',0,'POINT') + ' XAF'}"></td>
                </tr>
                <tr class="fw-semibold table-light">
                    <td>Salaire brut</td>
                    <td class="text-end" th:text="${#numbers.formatDecimal(bulletin.salaireBrut,1,'COMMA',0,'POINT') + ' XAF'}"></td>
                </tr>
                <tr class="text-danger">
                    <td>Cotisation CNPS employé (4,2%)</td>
                    <td class="text-end" th:text="${'− ' + #numbers.formatDecimal(bulletin.cnpsEmploye,1,'COMMA',0,'POINT') + ' XAF'}"></td>
                </tr>
                <tr class="text-danger">
                    <td>Avance sur salaire</td>
                    <td class="text-end" th:text="${'− ' + #numbers.formatDecimal(bulletin.avanceSurSalaire,1,'COMMA',0,'POINT') + ' XAF'}"></td>
                </tr>
                <tr class="fw-bold table-success">
                    <td>Net à payer</td>
                    <td class="text-end" th:text="${#numbers.formatDecimal(bulletin.salaireNet,1,'COMMA',0,'POINT') + ' XAF'}"></td>
                </tr>
                </tbody>
            </table>

            <!-- Section informatif CNPS patronal -->
            <div class="mt-3 p-3 bg-light rounded small text-muted">
                <strong>Information :</strong>
                Cotisation patronale CNPS (16,2%) =
                <span th:text="${#numbers.formatDecimal(bulletin.cnpsPatronal,1,'COMMA',0,'POINT') + ' XAF'}"></span>
                — à la charge de l'employeur, non déduite du net.
            </div>

            <!-- Infos paiement si payé -->
            <div th:if="${bulletin.statut.name() == 'PAYE'}" class="mt-3 alert alert-success small mb-0">
                <i class="fas fa-check-circle me-2"></i>
                Payé le <strong th:text="${#temporals.format(bulletin.datePaiement,'dd/MM/yyyy')}"></strong>.
                <span th:if="${bulletin.transaction != null}">
                    Transaction #<span th:text="${bulletin.transaction.id}"></span>
                </span>
            </div>
        </div>
    </div>

    <div class="mt-3">
        <a href="/bulletins" class="btn btn-outline-secondary btn-sm">
            <i class="fas fa-arrow-left me-1"></i>Retour aux bulletins
        </a>
    </div>
</div>
<div th:replace="~{fragments/layout :: scripts}"></div>
</body>
</html>
```

- [ ] **Step 5 : Créer `src/main/resources/templates/bulletins/mes-bulletins.html`**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/layout :: head('Mes bulletins')}"></head>
<body>
<nav th:replace="~{fragments/layout :: navbar}"></nav>
<div th:replace="~{fragments/layout :: flash-messages}"></div>

<div class="container mt-4">
    <h1 class="h4 fw-bold mb-4"><i class="fas fa-file-invoice-dollar me-2 text-primary"></i>Mes bulletins de paie</h1>

    <div th:if="${aucunProfil}" class="alert alert-info">
        <i class="fas fa-info-circle me-2"></i>Aucun profil employé associé à votre compte.
    </div>

    <div th:unless="${aucunProfil}">
        <p class="text-muted small mb-3" th:text="${employe.prenom + ' ' + employe.nom + ' — ' + employe.poste}"></p>
        <div class="card shadow-sm">
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead class="table-light">
                    <tr>
                        <th>Période</th>
                        <th class="text-end">Net</th>
                        <th>Statut</th>
                        <th class="text-end">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr th:each="b : ${bulletins}">
                        <td th:text="${#temporals.format(b.periode,'MMMM yyyy')}"></td>
                        <td class="text-end fw-semibold" th:text="${#numbers.formatDecimal(b.salaireNet,1,'COMMA',0,'POINT') + ' XAF'}"></td>
                        <td>
                            <span th:if="${b.statut.name() == 'GENERE'}" class="badge bg-warning text-dark">En attente</span>
                            <span th:if="${b.statut.name() == 'PAYE'}"   class="badge bg-success">Payé</span>
                        </td>
                        <td class="text-end">
                            <a th:href="@{/bulletins/{id}(id=${b.id})}" class="btn btn-outline-primary btn-sm">
                                <i class="fas fa-eye me-1"></i>Voir
                            </a>
                            <a th:href="@{/bulletins/{id}/pdf(id=${b.id})}" class="btn btn-outline-secondary btn-sm">
                                <i class="fas fa-file-pdf"></i>
                            </a>
                        </td>
                    </tr>
                    <tr th:if="${#lists.isEmpty(bulletins)}">
                        <td colspan="4" class="text-center text-muted py-4">Aucun bulletin disponible.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<div th:replace="~{fragments/layout :: scripts}"></div>
</body>
</html>
```

- [ ] **Step 6 : Compiler**

```
mvn compile -q
```
Attendu : BUILD SUCCESS

- [ ] **Step 7 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/controller/BulletinDePaieController.java
git add src/main/resources/templates/bulletins/
git commit -m "feat(rh): ajoute BulletinDePaieController et templates bulletins"
```

---

## Task 7 : Route PDF + template bulletin-template.html

**Files:**
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/controller/PdfController.java`
- Create: `src/main/resources/templates/employes/bulletin-template.html`

- [ ] **Step 1 : Ajouter les dépendances dans le constructeur de `PdfController`**

Ajouter `BulletinDePaieService` et `EmployeService` et `UserService` au constructeur existant (déjà injecté). Ajouter les champs :

```java
// Dans PdfController — nouveaux champs à ajouter (après les champs existants)
private final BulletinDePaieService bulletinDePaieService;
private final EmployeService employeService;
```

Ajouter dans le constructeur (après le paramètre `kpiService`) :
```java
                              BulletinDePaieService bulletinDePaieService,
                              EmployeService employeService
```

Et dans le corps :
```java
        this.bulletinDePaieService = bulletinDePaieService;
        this.employeService = employeService;
```

Ajouter l'import en tête du fichier :
```java
import lab.hang.Gestion.boulangerie.service.BulletinDePaieService;
import lab.hang.Gestion.boulangerie.service.EmployeService;
import lab.hang.Gestion.boulangerie.model.BulletinDePaie;
```

- [ ] **Step 2 : Ajouter la route `GET /bulletins/{id}/pdf` dans `PdfController`**

Ajouter la méthode suivante dans `PdfController` après la méthode `generateFacture` :

```java
    @GetMapping("/bulletins/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or @bulletinSecurity.isBulletinOwner(#id, authentication)")
    public void getBulletinPdf(@PathVariable Long id, HttpServletResponse response) throws Exception {
        BulletinDePaie bulletin = bulletinDePaieService.getById(id);

        Context context = new Context();
        context.setVariable("bulletin", bulletin);
        addBrandToContext(context);

        String html = templateEngine.process("employes/bulletin-template", context);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
            "attachment; filename=bulletin-" + bulletin.getEmploye().getNom()
            + "-" + bulletin.getPeriode() + ".pdf");

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

Ajouter l'import en tête du fichier :
```java
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
```

- [ ] **Step 3 : Créer `src/main/resources/templates/employes/bulletin-template.html`**

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
        .info-col  { display: table-cell; width: 50%; vertical-align: top; }
        .info-col p { margin: 2px 0; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 16px; }
        th, td { border: 1px solid #ccc; padding: 6px 10px; }
        th { background-color: #f5f5f5; text-align: left; }
        .text-right { text-align: right; }
        .total-row { font-weight: bold; background-color: #e8f5e9; }
        .deduction  { color: #c0392b; }
        .info-box   { background-color: #f5f5f5; padding: 10px; font-size: 11px; color: #666; border-radius: 4px; }
        .footer     { margin-top: 40px; display: table; width: 100%; }
        .footer-col { display: table-cell; width: 50%; }
    </style>
</head>
<body>

<div class="header">
    <h1 th:text="${appName ?: 'Boulangerie'}"></h1>
    <p>BULLETIN DE PAIE</p>
    <p><strong th:text="${#temporals.format(bulletin.periode,'MMMM yyyy')}"></strong></p>
</div>

<div class="info-grid">
    <div class="info-col">
        <p><strong>Employé :</strong> <span th:text="${bulletin.employe.prenom + ' ' + bulletin.employe.nom}"></span></p>
        <p><strong>Poste :</strong> <span th:text="${bulletin.employe.poste}"></span></p>
        <p><strong>N° CNPS :</strong> <span th:text="${bulletin.employe.numeroCnps ?: '—'}"></span></p>
    </div>
    <div class="info-col">
        <p><strong>Période :</strong> <span th:text="${#temporals.format(bulletin.periode,'MMMM yyyy')}"></span></p>
        <p><strong>Date de génération :</strong> <span th:text="${#temporals.format(bulletin.dateGeneration,'dd/MM/yyyy')}"></span></p>
        <p th:if="${bulletin.datePaiement != null}">
            <strong>Date de paiement :</strong>
            <span th:text="${#temporals.format(bulletin.datePaiement,'dd/MM/yyyy')}"></span>
        </p>
    </div>
</div>

<table>
    <thead>
    <tr>
        <th>Libellé</th>
        <th class="text-right">Montant</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>Salaire de base</td>
        <td class="text-right" th:text="${#numbers.formatDecimal(bulletin.salaireBase,1,'COMMA',0,'POINT') + ' XAF'}"></td>
    </tr>
    <tr>
        <td>Primes</td>
        <td class="text-right" th:text="${#numbers.formatDecimal(bulletin.primes,1,'COMMA',0,'POINT') + ' XAF'}"></td>
    </tr>
    <tr>
        <td>Indemnité de transport</td>
        <td class="text-right" th:text="${#numbers.formatDecimal(bulletin.indemnitesTransport,1,'COMMA',0,'POINT') + ' XAF'}"></td>
    </tr>
    <tr class="total-row">
        <td>Salaire brut</td>
        <td class="text-right" th:text="${#numbers.formatDecimal(bulletin.salaireBrut,1,'COMMA',0,'POINT') + ' XAF'}"></td>
    </tr>
    <tr class="deduction">
        <td>Cotisation CNPS employé (4,2%)</td>
        <td class="text-right" th:text="${'− ' + #numbers.formatDecimal(bulletin.cnpsEmploye,1,'COMMA',0,'POINT') + ' XAF'}"></td>
    </tr>
    <tr class="deduction">
        <td>Avance sur salaire</td>
        <td class="text-right" th:text="${'− ' + #numbers.formatDecimal(bulletin.avanceSurSalaire,1,'COMMA',0,'POINT') + ' XAF'}"></td>
    </tr>
    <tr class="total-row">
        <td><strong>Net à payer</strong></td>
        <td class="text-right"><strong th:text="${#numbers.formatDecimal(bulletin.salaireNet,1,'COMMA',0,'POINT') + ' XAF'}"></strong></td>
    </tr>
    </tbody>
</table>

<div class="info-box">
    Information : Cotisation patronale CNPS (16,2%) =
    <span th:text="${#numbers.formatDecimal(bulletin.cnpsPatronal,1,'COMMA',0,'POINT') + ' XAF'}"></span>
    — à la charge de l'employeur.
</div>

<div class="footer">
    <div class="footer-col">
        <p>Signature employé</p>
        <br/><br/>
        <p>__________________________</p>
    </div>
    <div class="footer-col" style="text-align:right">
        <p>Signature employeur</p>
        <br/><br/>
        <p>__________________________</p>
    </div>
</div>

</body>
</html>
```

- [ ] **Step 4 : Compiler**

```
mvn compile -q
```
Attendu : BUILD SUCCESS

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/controller/PdfController.java
git add src/main/resources/templates/employes/bulletin-template.html
git commit -m "feat(rh): ajoute route PDF bulletin de paie"
```

---

## Task 8 : SecurityConfig + Navbar + DataInitializer seed employés

**Files:**
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/security/SecurityConfig.java`
- Modify: `src/main/resources/templates/fragments/layout.html`
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/config/DataInitializer.java`

- [ ] **Step 1 : Modifier `SecurityConfig` — ajouter les règles employés/bulletins**

Dans `filterChain()`, ajouter avant `.anyRequest().authenticated()` :

```java
.requestMatchers(mvcMatcherBuilder.pattern("/employes/**")).hasAnyRole("ADMIN", "MANAGER")
.requestMatchers(mvcMatcherBuilder.pattern("/bulletins/**")).authenticated()
```

La règle `/bulletins/**` autorise l'accès à tout utilisateur authentifié ; la granularité par rôle est gérée via `@PreAuthorize` dans le contrôleur.

- [ ] **Step 2 : Modifier `layout.html` — ajouter liens dans la navbar**

Dans le dropdown **Admin** (déjà existant), ajouter après le lien `/admin/users` :
```html
<li>
    <a class="dropdown-item" href="/employes">
        <i class="fas fa-users me-2"></i>Employés
    </a>
</li>
```

Dans le dropdown **Rapports**, ajouter après le lien `/production/incidents` :
```html
<li sec:authorize="hasAnyRole('ADMIN','MANAGER')">
    <a class="dropdown-item" href="/bulletins">
        <i class="fas fa-file-invoice-dollar me-2 text-success"></i>Bulletins de paie
    </a>
</li>
```

Ajouter dans la navbar principale (pour tous les rôles), après l'item `/ventes-libres` ou dans un endroit visible pour les boulangers — un lien "Mes bulletins" :
```html
<li class="nav-item">
    <a class="nav-link rounded px-2" href="/bulletins/mes-bulletins">
        <i class="fas fa-file-invoice-dollar me-1"></i>Mes bulletins
    </a>
</li>
```

- [ ] **Step 3 : Modifier `DataInitializer` — seed employés**

Ajouter `EmployeRepository` comme champ et paramètre de constructeur :
```java
private final EmployeRepository employeRepository;
```

Dans le constructeur (après `appSettingsRepository`) :
```java
                           EmployeRepository employeRepository,
```
Corps :
```java
        this.employeRepository = employeRepository;
```

Ajouter l'appel dans `run()` après `initAppSettings()` :
```java
initEmployes();
```

Ajouter la méthode :
```java
private void initEmployes() {
    if (employeRepository.count() > 0) return;

    User boulanger1 = userRepository.findByUsername("boulanger1").orElse(null);
    User boulanger2 = userRepository.findByUsername("boulanger2").orElse(null);
    User manager    = userRepository.findByUsername("manager").orElse(null);

    employeRepository.saveAll(List.of(
        employe("Martin",  "Pierre",   "Boulanger",  80_000.0, boulanger1),
        employe("Nguema",  "Sylvie",   "Boulanger",  80_000.0, boulanger2),
        employe("Mbarga",  "Jacques",  "Manager",   120_000.0, manager)
    ));
}

private Employe employe(String nom, String prenom, String poste,
                        double salaireBase, User user) {
    Employe e = new Employe();
    e.setNom(nom);
    e.setPrenom(prenom);
    e.setPoste(poste);
    e.setSalaireBase(salaireBase);
    e.setDateEmbauche(LocalDate.now().minusYears(1));
    e.setActif(true);
    e.setUser(user);
    return e;
}
```

Ajouter l'import :
```java
import lab.hang.Gestion.boulangerie.model.Employe;
import lab.hang.Gestion.boulangerie.repository.EmployeRepository;
```

- [ ] **Step 4 : Lancer la suite de tests complète**

```
mvn test -q 2>&1 | tail -10
```
Attendu : BUILD SUCCESS — tous les tests passent (aucune régression)

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/security/SecurityConfig.java
git add src/main/resources/templates/fragments/layout.html
git add src/main/java/lab/hang/Gestion/boulangerie/config/DataInitializer.java
git commit -m "feat(rh): sécurité, navigation et seed employés — sous-projet B complet"
```

---

## Vérification finale

- [ ] `mvn test` passe sans régression
- [ ] `GET /employes` → liste des employés actifs (ADMIN/MANAGER uniquement)
- [ ] `GET /employes/nouveau` → formulaire création
- [ ] `POST /employes/nouveau` → crée l'employé, redirige vers `/employes`
- [ ] `GET /bulletins/generer` → formulaire avec aperçu JS
- [ ] `POST /bulletins/generer` → crée le bulletin, redirige vers `/bulletins/{id}`
- [ ] `GET /bulletins/{id}` → détail complet avec bouton Payer
- [ ] `POST /bulletins/{id}/payer` → crée la Transaction, met à jour le statut
- [ ] `GET /bulletins/{id}/pdf` → télécharge le PDF
- [ ] `GET /bulletins/mes-bulletins` → accessible à un BOULANGER lié
- [ ] `GET /bulletins/mes-bulletins` → message "Aucun profil" si pas d'employé lié
