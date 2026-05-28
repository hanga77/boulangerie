# Production : comparatif quantités et gestion des incidents — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Afficher le comparatif théorique/réel pendant la validation de production et permettre au boulanger de signaler un incident (avarie) dont l'impact stock est appliqué automatiquement selon le type.

**Architecture:** Deux nouvelles entités JPA (`AppSettings`, `IncidentProduction`) avec leurs services. La page `confirm.html` existante est enrichie avec un tableau comparatif et une section incident en JavaScript pur. La logique de création d'incidents s'insère dans `ProductionController.validerProduction()` sans modifier `ProductionService`.

**Tech Stack:** Spring Boot 3.4.2, Spring Data JPA (Hibernate), Thymeleaf, Bootstrap 5, Mockito + JUnit 5 (tests existants), MySQL.

---

## Carte des fichiers

**Créés :**
- `src/main/java/lab/hang/Gestion/boulangerie/model/TypeIncident.java`
- `src/main/java/lab/hang/Gestion/boulangerie/model/IncidentProduction.java`
- `src/main/java/lab/hang/Gestion/boulangerie/model/AppSettings.java`
- `src/main/java/lab/hang/Gestion/boulangerie/repository/IncidentProductionRepository.java`
- `src/main/java/lab/hang/Gestion/boulangerie/repository/AppSettingsRepository.java`
- `src/main/java/lab/hang/Gestion/boulangerie/service/IncidentProductionService.java`
- `src/main/java/lab/hang/Gestion/boulangerie/service/AppSettingsService.java`
- `src/main/resources/templates/production/incidents.html`
- `src/test/java/lab/hang/gestion_boulangerie/service/IncidentProductionServiceTest.java`
- `src/test/java/lab/hang/gestion_boulangerie/service/AppSettingsServiceTest.java`

**Modifiés :**
- `src/main/java/lab/hang/Gestion/boulangerie/model/StockMovement.java` — ajouter PERTE au @Pattern
- `src/main/java/lab/hang/Gestion/boulangerie/service/StockService.java` — ajouter `lostStock()`
- `src/main/java/lab/hang/Gestion/boulangerie/controller/ProductionController.java` — injecter seuil, traiter incidents
- `src/main/java/lab/hang/Gestion/boulangerie/controller/UserController.java` — endpoint seuil-incident
- `src/main/java/lab/hang/Gestion/boulangerie/config/DataInitializer.java` — seed AppSettings
- `src/main/java/lab/hang/Gestion/boulangerie/security/SecurityConfig.java` — restreindre /production/incidents
- `src/main/resources/templates/production/confirm.html` — tableau comparatif + section incident
- `src/main/resources/templates/production/details.html` — section incidents liés
- `src/main/resources/templates/admin/settings.html` — champ seuil

---

## Task 1 : AppSettings entity + repository + service + seed

**Files:**
- Create: `src/main/java/lab/hang/Gestion/boulangerie/model/AppSettings.java`
- Create: `src/main/java/lab/hang/Gestion/boulangerie/repository/AppSettingsRepository.java`
- Create: `src/main/java/lab/hang/Gestion/boulangerie/service/AppSettingsService.java`
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/config/DataInitializer.java`
- Create: `src/test/java/lab/hang/gestion_boulangerie/service/AppSettingsServiceTest.java`

- [ ] **Step 1 : Écrire le test**

```java
// src/test/java/lab/hang/gestion_boulangerie/service/AppSettingsServiceTest.java
package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.model.AppSettings;
import lab.hang.Gestion.boulangerie.repository.AppSettingsRepository;
import lab.hang.Gestion.boulangerie.service.AppSettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppSettingsServiceTest {

    @Mock AppSettingsRepository appSettingsRepository;
    @InjectMocks AppSettingsService appSettingsService;

    @Test
    void getSeuil_returnsStoredValue() {
        AppSettings setting = new AppSettings();
        setting.setCle("seuil_incident_production");
        setting.setValeur("15.0");
        when(appSettingsRepository.findById("seuil_incident_production"))
                .thenReturn(Optional.of(setting));

        double seuil = appSettingsService.getSeuilIncident();

        assertThat(seuil).isEqualTo(15.0);
    }

    @Test
    void getSeuil_returnsDefault10WhenAbsent() {
        when(appSettingsRepository.findById("seuil_incident_production"))
                .thenReturn(Optional.empty());

        double seuil = appSettingsService.getSeuilIncident();

        assertThat(seuil).isEqualTo(10.0);
    }

    @Test
    void updateSeuil_savesNewValue() {
        appSettingsService.updateSeuilIncident(20.0);

        verify(appSettingsRepository).save(argThat(s ->
            s.getCle().equals("seuil_incident_production") &&
            s.getValeur().equals("20.0")
        ));
    }
}
```

- [ ] **Step 2 : Lancer le test — vérifier qu'il échoue (AppSettingsService introuvable)**

```
mvn test -pl . -Dtest=AppSettingsServiceTest -q
```
Résultat attendu : COMPILATION ERROR (classes manquantes)

- [ ] **Step 3 : Créer AppSettings**

```java
// src/main/java/lab/hang/Gestion/boulangerie/model/AppSettings.java
package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class AppSettings {
    @Id
    private String cle;
    private String valeur;

    public AppSettings(String cle, String valeur) {
        this.cle = cle;
        this.valeur = valeur;
    }
}
```

- [ ] **Step 4 : Créer AppSettingsRepository**

```java
// src/main/java/lab/hang/Gestion/boulangerie/repository/AppSettingsRepository.java
package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingsRepository extends JpaRepository<AppSettings, String> {}
```

- [ ] **Step 5 : Créer AppSettingsService**

```java
// src/main/java/lab/hang/Gestion/boulangerie/service/AppSettingsService.java
package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.AppSettings;
import lab.hang.Gestion.boulangerie.repository.AppSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppSettingsService {

    private static final String SEUIL_KEY = "seuil_incident_production";
    private static final double SEUIL_DEFAULT = 10.0;

    private final AppSettingsRepository appSettingsRepository;

    public AppSettingsService(AppSettingsRepository appSettingsRepository) {
        this.appSettingsRepository = appSettingsRepository;
    }

    public double getSeuilIncident() {
        return appSettingsRepository.findById(SEUIL_KEY)
                .map(s -> Double.parseDouble(s.getValeur()))
                .orElse(SEUIL_DEFAULT);
    }

    @Transactional
    public void updateSeuilIncident(double seuil) {
        appSettingsRepository.save(new AppSettings(SEUIL_KEY, String.valueOf(seuil)));
    }
}
```

- [ ] **Step 6 : Lancer le test — vérifier qu'il passe**

```
mvn test -pl . -Dtest=AppSettingsServiceTest -q
```
Résultat attendu : BUILD SUCCESS, 3 tests passed

- [ ] **Step 7 : Seed dans DataInitializer**

Dans `DataInitializer.java` : injecter `AppSettingsRepository`, appeler `initAppSettings()` depuis `run()`.

```java
// Ajouter le champ et paramètre constructeur :
private final AppSettingsRepository appSettingsRepository;

// Dans le constructeur existant, ajouter :
this.appSettingsRepository = appSettingsRepository;

// Ajouter la méthode :
private void initAppSettings() {
    if (appSettingsRepository.count() > 0) return;
    appSettingsRepository.save(new AppSettings("seuil_incident_production", "10.0"));
}

// Dans run(), ajouter après initUsers() :
initAppSettings();
```

- [ ] **Step 8 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/model/AppSettings.java
git add src/main/java/lab/hang/Gestion/boulangerie/repository/AppSettingsRepository.java
git add src/main/java/lab/hang/Gestion/boulangerie/service/AppSettingsService.java
git add src/main/java/lab/hang/Gestion/boulangerie/config/DataInitializer.java
git add src/test/java/lab/hang/gestion_boulangerie/service/AppSettingsServiceTest.java
git commit -m "feat: AppSettings entity + service + seed seuil incident"
```

---

## Task 2 : TypeIncident + IncidentProduction + mise à jour StockMovement

**Files:**
- Create: `src/main/java/lab/hang/Gestion/boulangerie/model/TypeIncident.java`
- Create: `src/main/java/lab/hang/Gestion/boulangerie/model/IncidentProduction.java`
- Create: `src/main/java/lab/hang/Gestion/boulangerie/repository/IncidentProductionRepository.java`
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/model/StockMovement.java`

- [ ] **Step 1 : Créer TypeIncident**

```java
// src/main/java/lab/hang/Gestion/boulangerie/model/TypeIncident.java
package lab.hang.Gestion.boulangerie.model;

public enum TypeIncident {
    PATE_RATEE,
    FOURNEE_BRULEE,
    MATIERE_AVARIEE,
    PANNE_FOUR,
    SOUS_RENDEMENT,
    AUTRE
}
```

- [ ] **Step 2 : Créer IncidentProduction**

```java
// src/main/java/lab/hang/Gestion/boulangerie/model/IncidentProduction.java
package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
public class IncidentProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "production_id")
    private Production production;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeIncident type;

    @ManyToOne(optional = true)
    @JoinColumn(name = "produit_id")
    private Produit produitConcerne;

    @ManyToOne(optional = true)
    @JoinColumn(name = "matiere_id")
    private MatierePremiere matiereConcernee;

    private double quantitePerdue;

    @Column(length = 500)
    private String cause;

    @Column(nullable = false)
    private LocalDate dateIncident;

    private boolean stockAjuste;

    @ManyToOne(optional = false)
    @JoinColumn(name = "signaled_by")
    private User signaledBy;

    @OneToOne(optional = true)
    private StockMovement mouvementStock;
}
```

- [ ] **Step 3 : Créer IncidentProductionRepository**

```java
// src/main/java/lab/hang/Gestion/boulangerie/repository/IncidentProductionRepository.java
package lab.hang.Gestion.boulangerie.repository;

import lab.hang.Gestion.boulangerie.model.IncidentProduction;
import lab.hang.Gestion.boulangerie.model.TypeIncident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IncidentProductionRepository extends JpaRepository<IncidentProduction, Long> {
    List<IncidentProduction> findByProductionId(Long productionId);
    List<IncidentProduction> findByType(TypeIncident type);
    List<IncidentProduction> findByDateIncidentBetween(LocalDate debut, LocalDate fin);
    List<IncidentProduction> findByDateIncidentBetweenAndType(LocalDate debut, LocalDate fin, TypeIncident type);
}
```

- [ ] **Step 4 : Ajouter PERTE au @Pattern de StockMovement**

Dans `StockMovement.java`, remplacer la ligne :
```java
@Pattern(regexp = "^(ENTREE|SORTIE|RETOUR)$")
```
par :
```java
@Pattern(regexp = "^(ENTREE|SORTIE|RETOUR|PERTE)$")
```

- [ ] **Step 5 : Compiler et vérifier**

```
mvn compile -q
```
Résultat attendu : BUILD SUCCESS

- [ ] **Step 6 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/model/TypeIncident.java
git add src/main/java/lab/hang/Gestion/boulangerie/model/IncidentProduction.java
git add src/main/java/lab/hang/Gestion/boulangerie/repository/IncidentProductionRepository.java
git add src/main/java/lab/hang/Gestion/boulangerie/model/StockMovement.java
git commit -m "feat: TypeIncident enum + IncidentProduction entity + PERTE stock type"
```

---

## Task 3 : StockService.lostStock()

**Files:**
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/service/StockService.java`

`lostStock` est utilisé uniquement pour **MATIERE_AVARIEE** : la matière est en stock mais trouvée avariée, il faut la déduire du stock et tracer la perte. Pour les autres types (PATE_RATEE, etc.), la réconciliation théorique/réel de `ProductionService.updateProduction()` gère déjà le stock.

- [ ] **Step 1 : Ajouter lostStock() dans StockService**

À la fin de `StockService.java`, avant le dernier `}`, ajouter :

```java
/** Déduction de stock pour matière avariée (trouvée inutilisable avant/pendant production). */
@Transactional
public StockMovement lostStock(Long matierePremiereId, double quantite, String motif) {
    if (quantite <= 0) throw new IllegalArgumentException("La quantité doit être positive.");
    MatierePremiere matierePremiere = matierePremiereService.getMatierePremiereById(matierePremiereId);
    double newStock = Math.max(0, matierePremiere.getStock() - quantite);
    matierePremiere.setStock(newStock);
    matierePremiereService.saveMatierePremiere(matierePremiere);

    User currentUser = userService.getCurrentUser();
    StockMovement movement = new StockMovement();
    movement.setType("PERTE");
    movement.setQuantite(quantite);
    movement.setDate(LocalDate.now());
    movement.setMatierePremiere(matierePremiere);
    movement.setUser(currentUser);
    movement.setMotif(motif);
    return stockMovementRepository.save(movement);
}
```

- [ ] **Step 2 : Lancer les tests existants pour vérifier qu'il n'y a pas de régression**

```
mvn test -pl . -Dtest=StockServiceTest -q
```
Résultat attendu : BUILD SUCCESS

- [ ] **Step 3 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/service/StockService.java
git commit -m "feat: StockService.lostStock() pour matières avariées"
```

---

## Task 4 : IncidentProductionService

**Files:**
- Create: `src/main/java/lab/hang/Gestion/boulangerie/service/IncidentProductionService.java`
- Create: `src/test/java/lab/hang/gestion_boulangerie/service/IncidentProductionServiceTest.java`

- [ ] **Step 1 : Écrire le test**

```java
// src/test/java/lab/hang/gestion_boulangerie/service/IncidentProductionServiceTest.java
package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.IncidentProductionRepository;
import lab.hang.Gestion.boulangerie.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentProductionServiceTest {

    @Mock IncidentProductionRepository incidentRepository;
    @Mock StockService stockService;
    @Mock MatierePremiereService matierePremiereService;
    @Mock ProductionService productionService;
    @InjectMocks IncidentProductionService incidentService;

    @Test
    void creerIncident_MATIERE_AVARIEE_deduitStock() {
        Production production = new Production();
        production.setId(1L);
        production.setDateProduction(LocalDate.now());

        User boulanger = new User();
        boulanger.setId(1L);

        MatierePremiere farine = new MatierePremiere();
        farine.setId(2L);
        farine.setNom("Farine de blé");

        StockMovement movement = new StockMovement();
        movement.setId(10L);
        when(stockService.lostStock(2L, 5.0, "AVARIE: cause test")).thenReturn(movement);
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        IncidentProduction incident = incidentService.creerIncident(
                production, TypeIncident.MATIERE_AVARIEE, null, farine,
                5.0, "cause test", boulanger
        );

        assertThat(incident.isStockAjuste()).isTrue();
        assertThat(incident.getMouvementStock()).isEqualTo(movement);
        verify(stockService).lostStock(2L, 5.0, "AVARIE: cause test");
    }

    @Test
    void creerIncident_FOURNEE_BRULEE_neDeduitPasStock() {
        Production production = new Production();
        production.setId(1L);
        production.setDateProduction(LocalDate.now());

        User boulanger = new User();
        boulanger.setId(1L);

        Produit croissant = new Produit();
        croissant.setId(3L);

        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        IncidentProduction incident = incidentService.creerIncident(
                production, TypeIncident.FOURNEE_BRULEE, croissant, null,
                30.0, "four trop chaud", boulanger
        );

        assertThat(incident.isStockAjuste()).isFalse();
        assertThat(incident.getMouvementStock()).isNull();
        verifyNoInteractions(stockService);
    }

    @Test
    void getIncidentsByProduction_delegatesRepository() {
        when(incidentRepository.findByProductionId(1L)).thenReturn(List.of());
        List<IncidentProduction> result = incidentService.getIncidentsByProduction(1L);
        assertThat(result).isEmpty();
        verify(incidentRepository).findByProductionId(1L);
    }
}
```

- [ ] **Step 2 : Lancer le test — vérifier COMPILATION ERROR (classe manquante)**

```
mvn test -pl . -Dtest=IncidentProductionServiceTest -q
```

- [ ] **Step 3 : Implémenter IncidentProductionService**

```java
// src/main/java/lab/hang/Gestion/boulangerie/service/IncidentProductionService.java
package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.IncidentProductionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class IncidentProductionService {

    private final IncidentProductionRepository incidentRepository;
    private final StockService stockService;

    public IncidentProductionService(IncidentProductionRepository incidentRepository,
                                     StockService stockService) {
        this.incidentRepository = incidentRepository;
        this.stockService = stockService;
    }

    @Transactional
    public IncidentProduction creerIncident(Production production,
                                            TypeIncident type,
                                            Produit produitConcerne,
                                            MatierePremiere matiereConcernee,
                                            double quantitePerdue,
                                            String cause,
                                            User signaledBy) {
        IncidentProduction incident = new IncidentProduction();
        incident.setProduction(production);
        incident.setType(type);
        incident.setProduitConcerne(produitConcerne);
        incident.setMatiereConcernee(matiereConcernee);
        incident.setQuantitePerdue(quantitePerdue);
        incident.setCause(cause);
        incident.setDateIncident(production.getDateProduction());
        incident.setSignaledBy(signaledBy);
        incident.setStockAjuste(false);

        if (type == TypeIncident.MATIERE_AVARIEE && matiereConcernee != null) {
            StockMovement movement = stockService.lostStock(
                    matiereConcernee.getId(), quantitePerdue, "AVARIE: " + cause);
            incident.setMouvementStock(movement);
            incident.setStockAjuste(true);
        }

        return incidentRepository.save(incident);
    }

    public List<IncidentProduction> getIncidentsByProduction(Long productionId) {
        return incidentRepository.findByProductionId(productionId);
    }

    public List<IncidentProduction> getAllIncidents(LocalDate debut, LocalDate fin, TypeIncident type) {
        if (type != null) {
            return incidentRepository.findByDateIncidentBetweenAndType(debut, fin, type);
        }
        return incidentRepository.findByDateIncidentBetween(debut, fin);
    }
}
```

- [ ] **Step 4 : Lancer le test — vérifier qu'il passe**

```
mvn test -pl . -Dtest=IncidentProductionServiceTest -q
```
Résultat attendu : BUILD SUCCESS, 3 tests passed

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/service/IncidentProductionService.java
git add src/test/java/lab/hang/gestion_boulangerie/service/IncidentProductionServiceTest.java
git commit -m "feat: IncidentProductionService avec gestion MATIERE_AVARIEE"
```

---

## Task 5 : SecurityConfig + UserController (seuil-incident)

**Files:**
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/security/SecurityConfig.java`
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/controller/UserController.java`

- [ ] **Step 1 : Ajouter la règle /production/incidents dans SecurityConfig**

Dans `SecurityConfig.java`, avant `.anyRequest().authenticated()`, ajouter :

```java
.requestMatchers(mvcMatcherBuilder.pattern("/production/incidents")).hasAnyRole("ADMIN", "MANAGER")
```

- [ ] **Step 2 : Ajouter l'endpoint seuil-incident dans UserController**

Ajouter les imports en haut si absents :
```java
import lab.hang.Gestion.boulangerie.service.AppSettingsService;
```

Ajouter le champ dans `UserController` :
```java
private final AppSettingsService appSettingsService;
```

Mettre à jour le constructeur pour injecter `AppSettingsService`.

Ajouter la méthode dans `showSettings` pour passer le seuil au modèle :
```java
@GetMapping("/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
public String showSettings(Model model) {
    model.addAttribute("seuilIncident", appSettingsService.getSeuilIncident());
    return "admin/settings";
}
```

Ajouter l'endpoint POST :
```java
@PostMapping("/admin/settings/seuil-incident")
@PreAuthorize("hasRole('ADMIN')")
public String updateSeuilIncident(@RequestParam double seuil, RedirectAttributes ra) {
    if (seuil < 1 || seuil > 100) {
        ra.addFlashAttribute("errorMessage", "Le seuil doit être entre 1 et 100 %.");
        return "redirect:/admin/settings";
    }
    appSettingsService.updateSeuilIncident(seuil);
    ra.addFlashAttribute("successMessage", "Seuil d'alerte mis à jour : " + seuil + " %");
    return "redirect:/admin/settings";
}
```

- [ ] **Step 3 : Compiler**

```
mvn compile -q
```
Résultat attendu : BUILD SUCCESS

- [ ] **Step 4 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/security/SecurityConfig.java
git add src/main/java/lab/hang/Gestion/boulangerie/controller/UserController.java
git commit -m "feat: sécurité /production/incidents + endpoint seuil-incident"
```

---

## Task 6 : ProductionController — injecter seuil + traiter incidents

**Files:**
- Modify: `src/main/java/lab/hang/Gestion/boulangerie/controller/ProductionController.java`

- [ ] **Step 1 : Ajouter les dépendances au contrôleur**

Dans `ProductionController`, ajouter les champs :
```java
private final IncidentProductionService incidentProductionService;
private final AppSettingsService appSettingsService;
private final MatierePremiereService matierePremiereService2; // déjà présent via matierePremiereService
private final ProductionRepository productionRepository2; // utiliser productionService.getProductionById()
```

En réalité, injecter uniquement les deux nouveaux services dans le constructeur existant :
```java
private final IncidentProductionService incidentProductionService;
private final AppSettingsService appSettingsService;
```
Mettre à jour le constructeur en conséquence.

- [ ] **Step 2 : Injecter le seuil dans les méthodes qui retournent confirm.html**

Dans `passerALaProduction (POST)`, ajouter avant le `return` :
```java
model.addAttribute("seuil", appSettingsService.getSeuilIncident());
```

Dans `confirmerProduction (POST)`, ajouter avant le `return` :
```java
model.addAttribute("seuil", appSettingsService.getSeuilIncident());
```

- [ ] **Step 3 : Traiter les incidents dans validerProduction (POST)**

Remplacer la méthode `validerProduction` complète :

```java
@PreAuthorize("hasRole('BOULANGER')")
@PostMapping("/valider-production")
public String validerProduction(@RequestParam Map<String, String> formData,
                                RedirectAttributes ra) {
    ProductionDTO productionDTO = new ProductionDTO();
    Map<Long, Double> quantitesReelles = new HashMap<>();
    Map<Long, Integer> produitsProduits = new HashMap<>();

    formData.forEach((key, value) -> {
        if (key.startsWith("quantitesReellesMatieres[")) {
            Long id = Long.valueOf(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
            quantitesReelles.put(id, Double.valueOf(value));
        } else if (key.startsWith("quantitesReellesProduits[")) {
            Long id = Long.valueOf(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
            produitsProduits.put(id, Integer.valueOf(value));
        }
    });

    productionDTO.setId(Long.valueOf(formData.get("productionId")));
    productionDTO.setQuantitesReellesUtilisees(quantitesReelles);
    productionDTO.setProduitsProduits(produitsProduits);
    productionService.updateProduction(productionDTO);

    // Traiter l'incident si signalé
    if ("true".equals(formData.get("incidentSignaler"))) {
        try {
            Long productionId = Long.valueOf(formData.get("productionId"));
            ProductionDTO prod = productionService.getProductionById(productionId);
            Production productionEntity = new Production();
            productionEntity.setId(prod.getId());
            productionEntity.setDateProduction(prod.getDateProduction());

            TypeIncident type = TypeIncident.valueOf(formData.get("incidentType"));
            double quantitePerdue = Double.parseDouble(
                    formData.getOrDefault("incidentQuantitePerdue", "0"));
            String cause = formData.getOrDefault("incidentCause", "");
            User boulanger = userService.getCurrentUser();

            MatierePremiere matiere = null;
            String matiereIdStr = formData.get("incidentMatiereId");
            if (matiereIdStr != null && !matiereIdStr.isBlank()) {
                matiere = matierePremiereService.getMatierePremiereById(Long.valueOf(matiereIdStr));
            }

            Produit produit = null;
            String produitIdStr = formData.get("incidentProduitId");
            if (produitIdStr != null && !produitIdStr.isBlank()) {
                produit = produitService.getProduitEntityById(Long.valueOf(produitIdStr));
            }

            incidentProductionService.creerIncident(
                    productionEntity, type, produit, matiere, quantitePerdue, cause, boulanger);

            ra.addFlashAttribute("successMessage", "Production validée. Incident enregistré.");
        } catch (Exception e) {
            ra.addFlashAttribute("warningMessage",
                    "Production validée mais erreur lors de l'enregistrement de l'incident : " + e.getMessage());
        }
    }

    return "redirect:/production";
}
```

- [ ] **Step 4 : Ajouter la route GET /production/incidents**

```java
@GetMapping("/incidents")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public String listIncidents(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
        @RequestParam(required = false) String type,
        Model model) {

    if (debut == null) debut = LocalDate.now().withDayOfMonth(1);
    if (fin == null) fin = LocalDate.now();

    TypeIncident typeIncident = null;
    if (type != null && !type.isBlank()) {
        try { typeIncident = TypeIncident.valueOf(type); } catch (IllegalArgumentException ignored) {}
    }

    var incidents = incidentProductionService.getAllIncidents(debut, fin, typeIncident);
    long avecImpactStock = incidents.stream().filter(IncidentProduction::isStockAjuste).count();

    model.addAttribute("incidents", incidents);
    model.addAttribute("debut", debut);
    model.addAttribute("fin", fin);
    model.addAttribute("typeSelectionne", type);
    model.addAttribute("typesIncident", TypeIncident.values());
    model.addAttribute("totalIncidents", incidents.size());
    model.addAttribute("avecImpactStock", avecImpactStock);
    model.addAttribute("sansImpactStock", incidents.size() - avecImpactStock);

    return "production/incidents";
}
```

Ajouter les imports nécessaires :
```java
import lab.hang.Gestion.boulangerie.model.IncidentProduction;
import lab.hang.Gestion.boulangerie.model.TypeIncident;
import lab.hang.Gestion.boulangerie.service.IncidentProductionService;
import lab.hang.Gestion.boulangerie.service.AppSettingsService;
```

- [ ] **Step 5 : Ajouter détails production — injecter les incidents**

Dans `afficherDetailsProduction`, ajouter avant le `return` :
```java
model.addAttribute("incidents",
        incidentProductionService.getIncidentsByProduction(productionId));
```

- [ ] **Step 6 : Compiler**

```
mvn compile -q
```
Résultat attendu : BUILD SUCCESS

- [ ] **Step 7 : Commit**

```bash
git add src/main/java/lab/hang/Gestion/boulangerie/controller/ProductionController.java
git commit -m "feat: ProductionController injecte seuil, traite incidents, route /incidents"
```

---

## Task 7 : confirm.html — tableau comparatif + section incident

**Files:**
- Modify: `src/main/resources/templates/production/confirm.html`

- [ ] **Step 1 : Réécrire confirm.html**

Remplacer le contenu complet du fichier par :

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head th:replace="~{fragments/layout :: head('Confirmation de production')}"></head>
<body class="d-flex flex-column min-vh-100">
<nav th:replace="~{fragments/layout :: navbar}"></nav>
<div th:replace="~{fragments/layout :: flash-messages}"></div>

<main class="flex-grow-1 container-fluid px-3 px-md-4 py-4">

    <div class="d-flex align-items-center gap-2 mb-4">
        <div class="rounded-circle bg-warning bg-opacity-10 p-3">
            <i class="fas fa-clipboard-check fa-lg text-warning"></i>
        </div>
        <div>
            <h1 class="h4 fw-bold mb-0">Confirmation de production</h1>
            <p class="text-muted small mb-0">
                Date : <strong th:text="${production.dateProduction}"></strong>
            </p>
        </div>
    </div>

    <div th:if="${warning}" class="alert alert-warning d-flex align-items-center gap-2">
        <i class="fas fa-exclamation-triangle flex-shrink-0"></i>
        <span th:text="${warning}"></span>
    </div>

    <form th:action="@{/production/valider-production}" method="post"
          id="productionForm" th:data-seuil="${seuil}">
        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
        <input type="hidden" th:name="productionId" th:value="${production.id}"/>

        <!-- Tableau comparatif Matières -->
        <div class="card shadow-sm mb-4">
            <div class="card-header bg-warning bg-opacity-75 py-3">
                <h6 class="mb-0 fw-semibold"><i class="fas fa-leaf me-2"></i>Matières premières — théorique vs réel</h6>
            </div>
            <div class="table-responsive">
                <table class="table table-sm align-middle mb-0">
                    <thead class="table-light">
                    <tr>
                        <th>Matière</th>
                        <th class="text-end">Théorique</th>
                        <th style="width:160px">Réel utilisé</th>
                        <th class="text-end">Écart %</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr th:each="entry : ${production.matieresPremieresUtilisees}"
                        th:id="${'row-m-' + entry.key}">
                        <td>
                            <span th:text="${matieres.get(entry.key).nom}"></span>
                            <small class="text-muted ms-1" th:text="${matieres.get(entry.key).uniteMesure}"></small>
                        </td>
                        <td class="text-end fw-semibold"
                            th:text="${#numbers.formatDecimal(entry.value,1,'COMMA',2,'POINT')}"></td>
                        <td>
                            <input type="number" step="0.01" min="0"
                                   class="form-control form-control-sm matiere-input"
                                   th:name="${'quantitesReellesMatieres[' + entry.key + ']'}"
                                   th:value="${entry.value}"
                                   th:data-theorique="${entry.value}"
                                   th:data-id="${entry.key}"
                                   th:data-nom="${matieres.get(entry.key).nom}"
                                   th:data-type="matiere"/>
                        </td>
                        <td class="text-end" th:id="${'ecart-m-' + entry.key}">—</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Tableau comparatif Produits -->
        <div class="card shadow-sm mb-4">
            <div class="card-header bg-success bg-opacity-75 text-white py-3">
                <h6 class="mb-0 fw-semibold"><i class="fas fa-boxes me-2"></i>Produits — prévu vs réel</h6>
            </div>
            <div class="table-responsive">
                <table class="table table-sm align-middle mb-0">
                    <thead class="table-light">
                    <tr>
                        <th>Produit</th>
                        <th class="text-end">Prévu</th>
                        <th style="width:120px">Réel produit</th>
                        <th class="text-end">Écart %</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr th:each="entry : ${production.produitsProduits}"
                        th:id="${'row-p-' + entry.key}">
                        <td th:text="${produits.get(entry.key).nom}"></td>
                        <td class="text-end fw-semibold" th:text="${entry.value}"></td>
                        <td>
                            <input type="number" min="0"
                                   class="form-control form-control-sm produit-input"
                                   th:name="${'quantitesReellesProduits[' + entry.key + ']'}"
                                   th:value="${entry.value}"
                                   th:data-theorique="${entry.value}"
                                   th:data-id="${entry.key}"
                                   th:data-nom="${produits.get(entry.key).nom}"
                                   th:data-type="produit"/>
                        </td>
                        <td class="text-end" th:id="${'ecart-p-' + entry.key}">—</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Section incident (cachée par défaut) -->
        <div id="incidentSection" class="card border-danger mb-4 d-none">
            <div class="card-header bg-danger text-white">
                <div class="form-check mb-0">
                    <input class="form-check-input" type="checkbox" id="incidentSignalerCheck"
                           name="incidentSignaler" value="true">
                    <label class="form-check-label fw-semibold" for="incidentSignalerCheck">
                        <i class="fas fa-exclamation-triangle me-2"></i>Signaler un incident de production
                    </label>
                </div>
            </div>
            <div class="card-body" id="incidentBody" style="display:none">
                <div class="row g-3">
                    <div class="col-md-4">
                        <label class="form-label fw-semibold">Type d'incident</label>
                        <select name="incidentType" class="form-select" id="incidentType">
                            <option value="PATE_RATEE">Pâte ratée</option>
                            <option value="FOURNEE_BRULEE">Fournée brûlée</option>
                            <option value="MATIERE_AVARIEE">Matière avariée</option>
                            <option value="PANNE_FOUR">Panne de four</option>
                            <option value="SOUS_RENDEMENT">Sous-rendement</option>
                            <option value="AUTRE">Autre</option>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label fw-semibold">Élément concerné</label>
                        <input type="text" class="form-control" id="incidentElementNom"
                               placeholder="Auto-rempli" readonly/>
                        <input type="hidden" name="incidentMatiereId" id="incidentMatiereId"/>
                        <input type="hidden" name="incidentProduitId" id="incidentProduitId"/>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label fw-semibold">Quantité perdue</label>
                        <input type="number" step="0.01" min="0" name="incidentQuantitePerdue"
                               class="form-control" id="incidentQuantitePerdue" placeholder="0"/>
                    </div>
                    <div class="col-md-12">
                        <label class="form-label fw-semibold">Cause / description</label>
                        <textarea name="incidentCause" class="form-control" rows="2"
                                  placeholder="Décrivez brièvement l'incident..."></textarea>
                    </div>
                </div>
                <p class="text-muted small mt-2 mb-0">
                    <i class="fas fa-info-circle me-1"></i>
                    Pour une <strong>matière avariée</strong>, le stock sera automatiquement déduit.
                    Pour les autres types, l'incident est documentaire.
                </p>
            </div>
        </div>

        <div class="d-flex gap-2 mt-4">
            <a href="/production" class="btn btn-outline-secondary">
                <i class="fas fa-times me-1"></i>Annuler
            </a>
            <button type="submit" class="btn btn-success">
                <i class="fas fa-check me-1"></i>Valider la production
            </button>
        </div>
    </form>

</main>
<div th:replace="~{fragments/layout :: scripts}"></div>
<script>
(function () {
    const form = document.getElementById('productionForm');
    const seuil = parseFloat(form.dataset.seuil) || 10;
    const incidentSection = document.getElementById('incidentSection');
    const incidentBody = document.getElementById('incidentBody');
    const incidentSignalerCheck = document.getElementById('incidentSignalerCheck');

    function calculerEcart(theorique, reel) {
        if (theorique === 0) return 0;
        return Math.abs(reel - theorique) / theorique * 100;
    }

    function updateRow(input) {
        const theorique = parseFloat(input.dataset.theorique) || 0;
        const reel = parseFloat(input.value) || 0;
        const ecart = calculerEcart(theorique, reel);
        const type = input.dataset.type; // 'matiere' ou 'produit'
        const id = input.dataset.id;
        const prefix = type === 'matiere' ? 'm' : 'p';

        const row = document.getElementById('row-' + prefix + '-' + id);
        const ecartCell = document.getElementById('ecart-' + prefix + '-' + id);

        if (ecartCell) {
            ecartCell.textContent = ecart.toFixed(1) + ' %';
            ecartCell.className = 'text-end fw-semibold ' +
                (ecart > seuil ? 'text-danger' : 'text-success');
        }
        if (row) {
            row.classList.toggle('table-danger', ecart > seuil);
        }
    }

    function checkAnyExceeds() {
        let anyExceeds = false;
        let firstExceedInput = null;
        document.querySelectorAll('.matiere-input, .produit-input').forEach(input => {
            const theorique = parseFloat(input.dataset.theorique) || 0;
            const reel = parseFloat(input.value) || 0;
            if (calculerEcart(theorique, reel) > seuil) {
                anyExceeds = true;
                if (!firstExceedInput) firstExceedInput = input;
            }
        });
        if (anyExceeds) {
            incidentSection.classList.remove('d-none');
            if (firstExceedInput) {
                document.getElementById('incidentElementNom').value = firstExceedInput.dataset.nom;
                if (firstExceedInput.dataset.type === 'matiere') {
                    document.getElementById('incidentMatiereId').value = firstExceedInput.dataset.id;
                    document.getElementById('incidentProduitId').value = '';
                } else {
                    document.getElementById('incidentProduitId').value = firstExceedInput.dataset.id;
                    document.getElementById('incidentMatiereId').value = '';
                }
            }
        } else {
            incidentSection.classList.add('d-none');
            incidentSignalerCheck.checked = false;
            incidentBody.style.display = 'none';
        }
    }

    document.querySelectorAll('.matiere-input, .produit-input').forEach(input => {
        input.addEventListener('input', () => { updateRow(input); checkAnyExceeds(); });
    });

    incidentSignalerCheck.addEventListener('change', function () {
        incidentBody.style.display = this.checked ? 'block' : 'none';
    });
})();
</script>
</body>
</html>
```

- [ ] **Step 2 : Compiler**

```
mvn compile -q
```
Résultat attendu : BUILD SUCCESS

- [ ] **Step 3 : Commit**

```bash
git add src/main/resources/templates/production/confirm.html
git commit -m "feat: confirm.html comparatif théorique/réel + section incident"
```

---

## Task 8 : production/details.html — section incidents

**Files:**
- Modify: `src/main/resources/templates/production/details.html`

- [ ] **Step 1 : Lire le fichier actuel**

```
cat src/main/resources/templates/production/details.html
```

- [ ] **Step 2 : Ajouter la section incidents en bas de page**

Juste avant la balise `</main>` (ou avant `<div th:replace="~{fragments/layout :: scripts}">`) ajouter :

```html
<!-- Section incidents -->
<div class="card mt-4">
    <div class="card-header d-flex align-items-center gap-2">
        <i class="fas fa-exclamation-triangle text-warning"></i>
        <span class="fw-semibold">Incidents de production</span>
        <span class="badge bg-danger ms-1" th:if="${!#lists.isEmpty(incidents)}"
              th:text="${#lists.size(incidents)}"></span>
    </div>
    <div class="card-body p-0">
        <div th:if="${#lists.isEmpty(incidents)}" class="text-muted text-center py-3 small">
            <i class="fas fa-check-circle text-success me-1"></i>Aucun incident signalé pour cette production.
        </div>
        <div class="table-responsive" th:unless="${#lists.isEmpty(incidents)}">
            <table class="table table-sm mb-0">
                <thead class="table-light">
                <tr>
                    <th>Type</th>
                    <th>Élément</th>
                    <th class="text-end">Qté perdue</th>
                    <th>Cause</th>
                    <th>Signalé par</th>
                    <th class="text-center">Stock ajusté</th>
                </tr>
                </thead>
                <tbody>
                <tr th:each="inc : ${incidents}">
                    <td>
                        <span class="badge"
                              th:classappend="${inc.type.name() == 'MATIERE_AVARIEE' ? 'bg-danger' :
                                              inc.type.name() == 'FOURNEE_BRULEE' ? 'bg-warning text-dark' :
                                              inc.type.name() == 'PATE_RATEE' ? 'bg-orange text-dark' : 'bg-secondary'}"
                              th:text="${inc.type.name().replace('_',' ')}"></span>
                    </td>
                    <td>
                        <span th:if="${inc.produitConcerne != null}" th:text="${inc.produitConcerne.nom}"></span>
                        <span th:if="${inc.matiereConcernee != null}" th:text="${inc.matiereConcernee.nom}"></span>
                        <span th:if="${inc.produitConcerne == null and inc.matiereConcernee == null}" class="text-muted">—</span>
                    </td>
                    <td class="text-end" th:text="${inc.quantitePerdue}"></td>
                    <td class="text-muted small" th:text="${inc.cause}"></td>
                    <td th:text="${inc.signaledBy.username}"></td>
                    <td class="text-center">
                        <i th:if="${inc.stockAjuste}" class="fas fa-check-circle text-success"></i>
                        <i th:unless="${inc.stockAjuste}" class="fas fa-minus text-muted"></i>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>
```

- [ ] **Step 3 : Compiler**

```
mvn compile -q
```
Résultat attendu : BUILD SUCCESS

- [ ] **Step 4 : Commit**

```bash
git add src/main/resources/templates/production/details.html
git commit -m "feat: details.html affiche la liste des incidents liés à la production"
```

---

## Task 9 : production/incidents.html (nouvelle page)

**Files:**
- Create: `src/main/resources/templates/production/incidents.html`

- [ ] **Step 1 : Créer incidents.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head th:replace="~{fragments/layout :: head('Incidents de production')}"></head>
<body class="d-flex flex-column min-vh-100">
<nav th:replace="~{fragments/layout :: navbar}"></nav>
<div th:replace="~{fragments/layout :: flash-messages}"></div>

<main class="container-fluid px-3 px-md-4 py-4 flex-grow-1">

    <div class="page-header mb-4">
        <div>
            <h1 class="h4 fw-bold mb-0">
                <i class="fas fa-exclamation-triangle me-2 text-warning"></i>Incidents de production
            </h1>
            <p class="text-muted small mb-0">Historique des avaries et pertes signalées</p>
        </div>
    </div>

    <!-- Filtres -->
    <div class="card mb-4">
        <div class="card-body">
            <form method="get" th:action="@{/production/incidents}" class="row g-3 align-items-end">
                <div class="col-md-3">
                    <label class="form-label small fw-semibold">Du</label>
                    <input type="date" name="debut" class="form-control form-control-sm"
                           th:value="${debut}"/>
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-semibold">Au</label>
                    <input type="date" name="fin" class="form-control form-control-sm"
                           th:value="${fin}"/>
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-semibold">Type</label>
                    <select name="type" class="form-select form-select-sm">
                        <option value="">Tous les types</option>
                        <option th:each="t : ${typesIncident}"
                                th:value="${t.name()}"
                                th:text="${t.name().replace('_',' ')}"
                                th:selected="${t.name() == typeSelectionne}"></option>
                    </select>
                </div>
                <div class="col-md-3">
                    <button type="submit" class="btn btn-primary btn-sm w-100">
                        <i class="fas fa-filter me-1"></i>Filtrer
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- KPI cards -->
    <div class="row g-3 mb-4">
        <div class="col-md-4">
            <div class="card text-center">
                <div class="card-body">
                    <div class="fs-2 fw-bold" th:text="${totalIncidents}">0</div>
                    <div class="text-muted small">Total incidents</div>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card text-center border-danger">
                <div class="card-body">
                    <div class="fs-2 fw-bold text-danger" th:text="${avecImpactStock}">0</div>
                    <div class="text-muted small">Avec déduction stock</div>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card text-center">
                <div class="card-body">
                    <div class="fs-2 fw-bold text-secondary" th:text="${sansImpactStock}">0</div>
                    <div class="text-muted small">Documentaires (sans déduction)</div>
                </div>
            </div>
        </div>
    </div>

    <!-- Tableau -->
    <div class="card">
        <div class="table-responsive">
            <table class="table table-hover mb-0">
                <thead class="table-light">
                <tr>
                    <th>Date</th>
                    <th>Production</th>
                    <th>Type</th>
                    <th>Élément concerné</th>
                    <th class="text-end">Qté perdue</th>
                    <th>Cause</th>
                    <th>Boulanger</th>
                    <th class="text-center">Stock</th>
                </tr>
                </thead>
                <tbody>
                <tr th:each="inc : ${incidents}">
                    <td th:text="${#temporals.format(inc.dateIncident,'dd/MM/yyyy')}"></td>
                    <td>
                        <a th:href="@{/production/details/{id}(id=${inc.production.id})}"
                           th:text="${'#' + inc.production.id}"></a>
                    </td>
                    <td>
                        <span class="badge"
                              th:classappend="${inc.type.name() == 'MATIERE_AVARIEE' ? 'bg-danger' :
                                              inc.type.name() == 'FOURNEE_BRULEE' ? 'bg-warning text-dark' :
                                              inc.type.name() == 'PATE_RATEE' ? 'bg-danger bg-opacity-75' : 'bg-secondary'}"
                              th:text="${inc.type.name().replace('_',' ')}"></span>
                    </td>
                    <td>
                        <span th:if="${inc.produitConcerne != null}" th:text="${inc.produitConcerne.nom}"></span>
                        <span th:if="${inc.matiereConcernee != null}" th:text="${inc.matiereConcernee.nom}"></span>
                        <span th:if="${inc.produitConcerne == null and inc.matiereConcernee == null}" class="text-muted">—</span>
                    </td>
                    <td class="text-end" th:text="${inc.quantitePerdue}"></td>
                    <td class="small text-muted" th:text="${inc.cause}"></td>
                    <td th:text="${inc.signaledBy.username}"></td>
                    <td class="text-center">
                        <i th:if="${inc.stockAjuste}" class="fas fa-check-circle text-success" title="Stock déduit"></i>
                        <i th:unless="${inc.stockAjuste}" class="fas fa-minus text-muted" title="Sans impact stock"></i>
                    </td>
                </tr>
                <tr th:if="${#lists.isEmpty(incidents)}">
                    <td colspan="8" class="text-center text-muted py-4">
                        <i class="fas fa-check-circle text-success fa-2x d-block mb-2 opacity-50"></i>
                        Aucun incident sur cette période.
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

</main>
<div th:replace="~{fragments/layout :: scripts}"></div>
</body>
</html>
```

- [ ] **Step 2 : Commit**

```bash
git add src/main/resources/templates/production/incidents.html
git commit -m "feat: page /production/incidents avec filtres et KPIs"
```

---

## Task 10 : admin/settings.html — champ seuil

**Files:**
- Modify: `src/main/resources/templates/admin/settings.html`

- [ ] **Step 1 : Ajouter la section seuil dans admin/settings.html**

Juste avant `</main>`, ajouter une nouvelle carte :

```html
<!-- Seuil incident production -->
<div class="card mb-4">
    <div class="card-header">
        <i class="fas fa-exclamation-triangle me-2 text-warning"></i>Production — seuil d'alerte incident
    </div>
    <div class="card-body">
        <p class="text-muted small mb-3">
            Quand l'écart entre quantité théorique et réelle dépasse ce seuil (en %),
            la section "Signaler un incident" apparaît automatiquement lors de la validation.
        </p>
        <form th:action="@{/admin/settings/seuil-incident}" method="post" class="row g-3 align-items-end">
            <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
            <div class="col-md-4">
                <label for="seuil" class="form-label fw-semibold">Seuil d'écart (%)</label>
                <div class="input-group">
                    <input type="number" id="seuil" name="seuil" class="form-control"
                           min="1" max="100" step="1"
                           th:value="${seuilIncident != null ? seuilIncident : 10}"/>
                    <span class="input-group-text">%</span>
                </div>
            </div>
            <div class="col-md-3">
                <button type="submit" class="btn btn-primary btn-sm">
                    <i class="fas fa-save me-1"></i>Enregistrer
                </button>
            </div>
        </form>
    </div>
</div>
```

- [ ] **Step 2 : Compiler**

```
mvn compile -q
```
Résultat attendu : BUILD SUCCESS

- [ ] **Step 3 : Commit**

```bash
git add src/main/resources/templates/admin/settings.html
git commit -m "feat: settings.html champ seuil incident production"
```

---

## Task 11 : Lien navbar + vérification finale

**Files:**
- Modify: `src/main/resources/templates/fragments/layout.html`

- [ ] **Step 1 : Ajouter le lien Incidents dans la navbar sous Production**

Dans le dropdown "Rapports" (ou ajouter directement sous le lien `/production`), ajouter un lien accessible aux ADMIN/MANAGER :

Dans `layout.html`, dans le dropdown **Rapports**, ajouter :
```html
<li sec:authorize="hasAnyRole('ADMIN','MANAGER')">
    <a class="dropdown-item" href="/production/incidents">
        <i class="fas fa-exclamation-triangle me-2 text-warning"></i>Incidents production
    </a>
</li>
```

- [ ] **Step 2 : Lancer tous les tests**

```
mvn test -q
```
Résultat attendu : BUILD SUCCESS, tous les tests passent

- [ ] **Step 3 : Commit final**

```bash
git add src/main/resources/templates/fragments/layout.html
git commit -m "feat: lien Incidents production dans la navbar"
git push origin master
```

---

## Auto-review du plan

**Couverture spec → tâches :**
| Exigence spec | Tâche |
|---|---|
| AppSettings + seuil configurable | Task 1 |
| TypeIncident enum + IncidentProduction entity | Task 2 |
| StockMovement type PERTE | Task 2 step 4 |
| StockService.lostStock() pour MATIERE_AVARIEE | Task 3 |
| IncidentProductionService.creerIncident() | Task 4 |
| /production/valider-production traite incidents | Task 6 step 3 |
| confirm.html tableau comparatif + JS temps réel | Task 7 |
| confirm.html section incident optionnelle | Task 7 |
| details.html section incidents | Task 8 |
| /production/incidents page filtrée | Task 6 step 4 + Task 9 |
| admin/settings.html champ seuil | Task 5 + Task 10 |
| Sécurité /production/incidents ADMIN/MANAGER | Task 5 step 1 |

**Cohérence des types :**
- `IncidentProductionService.creerIncident()` utilise `Production` (entity), `TypeIncident`, `Produit`, `MatierePremiere`, `User` — tous définis en Task 2/1.
- `StockService.lostStock()` retourne `StockMovement` — utilisé dans `creerIncident` pour `incident.setMouvementStock()`.
- `ProductionController` appelle `incidentProductionService.creerIncident(productionEntity, ...)` avec `productionEntity` construit depuis `ProductionDTO` — cohérent.
- `AppSettingsService.getSeuilIncident()` retourne `double` — injecté dans `model.addAttribute("seuil", ...)` et récupéré en `form.dataset.seuil` en JS.

**Placeholders :** aucun TBD/TODO dans le plan. ✓
