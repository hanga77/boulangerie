package lab.hang.Gestion.boulangerie.infrastructure.persistence;

import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneMatiere;
import lab.hang.Gestion.boulangerie.domain.production.ProductionLigneProduit;
import lab.hang.Gestion.boulangerie.domain.production.ProductionStatus;
import lab.hang.Gestion.boulangerie.domain.production.ProductionV2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4 P3 — persistance ProductionV2 + lignes sur H2.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@EntityScan(basePackages = "lab.hang.Gestion.boulangerie.domain.production")
@EnableJpaRepositories(
        basePackageClasses = ProductionV2Repository.class,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ProductionV2Repository.class
        )
)
class ProductionV2RepositoryDataJpaTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProductionV2Repository repository;

    @Test
    void persist_cascade_lignes_et_relecture() {
        ProductionV2 p = new ProductionV2(LocalDate.of(2026, 10, 26), 42L);
        p.ajouterLigneProduit(10L, 100);
        p.ajouterLigneProduit(11L, 50);
        p.ajouterOuCumulerMatiere(1L, 25.0);
        p.ajouterOuCumulerMatiere(1L, 5.0); // farinage cumulé

        ProductionV2 saved = repository.saveAndFlush(p);
        assertNotNull(saved.getId());

        em.clear();

        Optional<ProductionV2> loaded = repository.findByIdWithLignes(saved.getId());
        assertTrue(loaded.isPresent());

        ProductionV2 found = loaded.get();
        assertEquals(ProductionStatus.EN_COURS, found.getStatut());
        assertEquals(42L, found.getUserId());
        assertEquals(2, found.getLignesProduits().size());
        assertEquals(1, found.getLignesMatieres().size());
        assertEquals(30.0, found.getLignesMatieres().get(0).getQuantiteTheorique(), 0.001);

        ProductionLigneProduit pain = found.ligneProduit(10L).orElseThrow();
        assertEquals(100, pain.getQuantitePlanifiee());
        assertEquals(100, pain.getQuantiteRestante());
    }

    @Test
    void find_by_date_production() {
        LocalDate day = LocalDate.of(2026, 10, 26);
        ProductionV2 p = new ProductionV2(day, 1L);
        p.ajouterLigneProduit(1L, 10);
        repository.saveAndFlush(p);
        em.clear();

        assertTrue(repository.existsByDateProduction(day));
        Optional<ProductionV2> byDate = repository.findByDateProductionWithLignes(day);
        assertTrue(byDate.isPresent());
        assertEquals(1, byDate.get().getLignesProduits().size());
    }

    @Test
    void valider_puis_debiter_restant_persiste() {
        ProductionV2 p = new ProductionV2(LocalDate.now(), 1L);
        p.ajouterLigneProduit(99L, 40);
        p.valider();

        ProductionLigneProduit ligne = p.ligneProduit(99L).orElseThrow();
        ligne.debiterRestant(15);

        ProductionV2 saved = repository.saveAndFlush(p);
        em.clear();

        ProductionV2 found = repository.findByIdWithLignes(saved.getId()).orElseThrow();
        assertEquals(ProductionStatus.VALIDEE, found.getStatut());
        assertEquals(25, found.ligneProduit(99L).orElseThrow().getQuantiteRestante());
    }

    @Test
    void confirmer_magasinier_sur_ligne_matiere() {
        ProductionV2 p = new ProductionV2(LocalDate.now(), 1L);
        p.ajouterOuCumulerMatiere(7L, 12.5);
        ProductionLigneMatiere lm = p.ligneMatiere(7L).orElseThrow();
        lm.confirmerMagasinier(12.0);

        repository.saveAndFlush(p);
        em.clear();

        ProductionV2 found = repository.findByIdWithLignes(p.getId()).orElseThrow();
        assertEquals(12.0, found.ligneMatiere(7L).orElseThrow().getQuantiteMagasinier(), 0.001);
    }
}
