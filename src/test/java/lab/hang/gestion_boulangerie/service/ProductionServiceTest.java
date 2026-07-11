package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.CommandeDTO;
import lab.hang.Gestion.boulangerie.dto.ProductionDTO;
import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.exception.ProductionNotFoundException;
import lab.hang.Gestion.boulangerie.mapper.CommandeMapper;
import lab.hang.Gestion.boulangerie.mapper.ProductionMapper;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.*;
import lab.hang.Gestion.boulangerie.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionServiceTest {

    @Mock private ProductionRepository productionRepository;
    @Mock private MatierePremiereRepository matierePremiereRepository;
    @Mock private CommandeRepository commandeRepository;
    @Mock private CommandeService commandeService;
    @Mock private ProductionMapper productionMapper;
    @Mock private MatierePremiereService matierePremiereService;
    @Mock private ProduitService produitService;
    @Mock private CommandeMapper commandeMapper;
    @Mock private CompteBancaireRepository compteBancaireRepository;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks
    private ProductionService productionService;

    private User user;
    private MatierePremiere farine;
    private ProduitDTO painDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("boulanger");

        farine = new MatierePremiere();
        farine.setId(10L);
        farine.setNom("Farine");
        farine.setStock(500.0);
        farine.setPrixUnitaire(1.5);

        painDTO = new ProduitDTO();
        painDTO.setId(1L);
        painDTO.setNom("Pain");
        painDTO.setPrix(200.0);
        painDTO.setQuantiteVenteLibreJournaliere(0);
        painDTO.setMatieresPremieres(Map.of(10L, 0.5));
    }

    // ── startProduction ────────────────────────────────────────────────────

    @Test
    void startProduction_sans_commande_sans_quota_cree_production_vide() {
        when(commandeService.getCommandesByDateAndEtat(any())).thenReturn(List.of());
        when(produitService.getAllProduits()).thenReturn(List.of());

        Production savedProduction = new Production();
        savedProduction.setId(1L);
        when(productionMapper.toEntity(any())).thenReturn(savedProduction);
        when(productionRepository.save(any())).thenReturn(savedProduction);
        when(productionMapper.toDTO(savedProduction)).thenReturn(new ProductionDTO());

        ProductionDTO result = productionService.startProduction(LocalDate.now(), user);

        assertThat(result).isNotNull();
        verify(productionRepository).save(any());
    }

    @Test
    void startProduction_avec_commande_calcule_le_theorique_des_matieres() {
        // Une commande : 10 pains, chaque pain nécessite 0.5kg de farine → 5kg total.
        // startProduction ne débite plus le stock directement : le magasinier confirme
        // ensuite la sortie réelle via /matieres-premieres, en se basant sur ce théorique.
        CommandeDTO commande = new CommandeDTO();
        commande.setId(1L);
        commande.setProduitsCommandes(Map.of(1L, 10));

        when(commandeService.getCommandesByDateAndEtat(any())).thenReturn(List.of(commande));
        when(produitService.getProduitById(1L)).thenReturn(painDTO);
        when(produitService.calculateMatieresPremieresNecessaires(painDTO, 10))
                .thenReturn(Map.of(farine, 5.0));
        when(produitService.getAllProduits()).thenReturn(List.of());

        Production savedProduction = new Production();
        savedProduction.setId(42L);
        when(productionMapper.toEntity(any())).thenReturn(savedProduction);
        when(productionRepository.save(any())).thenReturn(savedProduction);
        when(productionMapper.toDTO(savedProduction)).thenReturn(new ProductionDTO());

        Commande commandeEntity = new Commande();
        when(commandeMapper.toEntity(commande)).thenReturn(commandeEntity);

        productionService.startProduction(LocalDate.now(), user);

        ArgumentCaptor<ProductionDTO> captor = ArgumentCaptor.forClass(ProductionDTO.class);
        verify(productionMapper).toEntity(captor.capture());
        assertThat(captor.getValue().getMatieresPremieresUtilisees()).containsEntry(10L, 5.0);
    }

    @Test
    void startProduction_avec_quota_vente_libre_inclut_la_production_supplementaire() {
        ProduitDTO painAvecQuota = new ProduitDTO();
        painAvecQuota.setId(2L);
        painAvecQuota.setNom("Pain spécial");
        painAvecQuota.setPrix(300.0);
        painAvecQuota.setQuantiteVenteLibreJournaliere(20); // quota de 20 unités
        painAvecQuota.setMatieresPremieres(Map.of(10L, 0.3));

        when(commandeService.getCommandesByDateAndEtat(any())).thenReturn(List.of());
        when(produitService.getAllProduits()).thenReturn(List.of(painAvecQuota));
        when(produitService.calculateMatieresPremieresNecessaires(painAvecQuota, 20))
                .thenReturn(Map.of(farine, 6.0));

        Production savedProduction = new Production();
        savedProduction.setId(1L);
        when(productionMapper.toEntity(any())).thenReturn(savedProduction);
        when(productionRepository.save(any())).thenReturn(savedProduction);
        when(productionMapper.toDTO(any())).thenReturn(new ProductionDTO());

        productionService.startProduction(LocalDate.now(), user);

        ArgumentCaptor<ProductionDTO> captor = ArgumentCaptor.forClass(ProductionDTO.class);
        verify(productionMapper).toEntity(captor.capture());
        assertThat(captor.getValue().getMatieresPremieresUtilisees()).containsEntry(10L, 6.0);
    }

    @Test
    void startProduction_avec_farinage_ajoute_au_theorique_de_la_matiere() {
        // Une commande : 10 pains → 5kg de farine théorique. Farinage manuel : 2kg de farine
        // supplémentaires pour le pétrissage → total attendu 7kg, pas lié à un produit.
        CommandeDTO commande = new CommandeDTO();
        commande.setId(1L);
        commande.setProduitsCommandes(Map.of(1L, 10));

        when(commandeService.getCommandesByDateAndEtat(any())).thenReturn(List.of(commande));
        when(produitService.getProduitById(1L)).thenReturn(painDTO);
        when(produitService.calculateMatieresPremieresNecessaires(painDTO, 10))
                .thenReturn(Map.of(farine, 5.0));
        when(produitService.getAllProduits()).thenReturn(List.of());

        Production savedProduction = new Production();
        savedProduction.setId(42L);
        when(productionMapper.toEntity(any())).thenReturn(savedProduction);
        when(productionRepository.save(any())).thenReturn(savedProduction);
        when(productionMapper.toDTO(savedProduction)).thenReturn(new ProductionDTO());

        Commande commandeEntity = new Commande();
        when(commandeMapper.toEntity(commande)).thenReturn(commandeEntity);

        productionService.startProduction(LocalDate.now(), user, 10L, 2.0);

        ArgumentCaptor<ProductionDTO> captor = ArgumentCaptor.forClass(ProductionDTO.class);
        verify(productionMapper).toEntity(captor.capture());
        assertThat(captor.getValue().getMatieresPremieresUtilisees()).containsEntry(10L, 7.0);
    }

    @Test
    void startProduction_sans_farinage_ne_modifie_pas_le_theorique() {
        when(commandeService.getCommandesByDateAndEtat(any())).thenReturn(List.of());
        when(produitService.getAllProduits()).thenReturn(List.of());

        Production savedProduction = new Production();
        savedProduction.setId(1L);
        when(productionMapper.toEntity(any())).thenReturn(savedProduction);
        when(productionRepository.save(any())).thenReturn(savedProduction);
        when(productionMapper.toDTO(any())).thenReturn(new ProductionDTO());

        productionService.startProduction(LocalDate.now(), user, null, null);

        ArgumentCaptor<ProductionDTO> captor = ArgumentCaptor.forClass(ProductionDTO.class);
        verify(productionMapper).toEntity(captor.capture());
        assertThat(captor.getValue().getMatieresPremieresUtilisees()).isEmpty();
    }

    // ── updateProduction (réconciliation stock) ────────────────────────────

    @Test
    void updateProduction_enregistre_les_quantites_reelles_utilisees() {
        // updateProduction ne touche pas au stock (MatierePremiere.stock) : il enregistre
        // uniquement le réel déclaré par le boulanger sur la Production, à titre de référence
        // pour le magasinier qui confirmera ensuite la sortie de stock réelle.
        Production production = new Production();
        production.setId(5L);
        Map<MatierePremiere, Double> theorique = new HashMap<>();
        theorique.put(farine, 100.0);
        production.setMatieresPremieresUtilisees(theorique);

        when(productionRepository.findById(5L)).thenReturn(Optional.of(production));
        when(matierePremiereService.getMatierePremiereById(10L)).thenReturn(farine);

        Produit pain = new Produit();
        pain.setId(1L);
        when(produitService.getProduitEntityById(1L)).thenReturn(pain);

        ProductionDTO dto = new ProductionDTO();
        dto.setId(5L);
        dto.setQuantitesReellesUtilisees(Map.of(10L, 80.0)); // 80 réel vs 100 théorique
        dto.setProduitsProduits(Map.of(1L, 50));

        productionService.updateProduction(dto);

        assertThat(production.getQuantitesReellesUtilisees()).containsEntry(farine, 80.0);
        assertThat(production.getProduitsProduits()).containsEntry(pain, 50);
        assertThat(production.getProduitsRestants()).containsEntry(pain, 50);
        verify(productionRepository).save(production);
        assertThat(farine.getStock()).isEqualTo(500.0); // stock inchangé — pas de débit automatique
    }

    @Test
    void updateProduction_id_inexistant_leve_ProductionNotFoundException() {
        when(productionRepository.findById(99L)).thenReturn(Optional.empty());

        ProductionDTO dto = new ProductionDTO();
        dto.setId(99L);
        dto.setQuantitesReellesUtilisees(Map.of());
        dto.setProduitsProduits(Map.of());

        assertThatThrownBy(() -> productionService.updateProduction(dto))
                .isInstanceOf(ProductionNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── verifierStocksSuffisants ───────────────────────────────────────────

    @Test
    void verifierStocksSuffisants_retourne_true_quand_stock_ok() {
        farine.setStock(100.0);
        when(matierePremiereRepository.findById(10L)).thenReturn(Optional.of(farine));

        ProductionDTO dto = new ProductionDTO();
        dto.setMatieresPremieresUtilisees(Map.of(10L, 50.0));

        assertThat(productionService.verifierStocksSuffisants(dto)).isTrue();
    }

    @Test
    void verifierStocksSuffisants_retourne_false_quand_stock_insuffisant() {
        farine.setStock(30.0);
        when(matierePremiereRepository.findById(10L)).thenReturn(Optional.of(farine));

        ProductionDTO dto = new ProductionDTO();
        dto.setMatieresPremieresUtilisees(Map.of(10L, 50.0));

        assertThat(productionService.verifierStocksSuffisants(dto)).isFalse();
    }

    // ── calculerCoutProduction ─────────────────────────────────────────────

    @Test
    void calculerCoutProduction_retourne_somme_quantite_x_prix_unitaire() {
        MatierePremiere levure = new MatierePremiere();
        levure.setId(11L);
        levure.setNom("Levure");
        levure.setPrixUnitaire(5.0);

        Production production = new Production();
        production.setId(3L);
        Map<MatierePremiere, Double> matieres = new HashMap<>();
        matieres.put(farine, 10.0);   // 10 * 1.5 = 15
        matieres.put(levure, 2.0);    //  2 * 5.0 = 10
        production.setMatieresPremieresUtilisees(matieres);

        when(productionRepository.findById(3L)).thenReturn(Optional.of(production));

        double cout = productionService.calculerCoutProduction(3L);

        assertThat(cout).isEqualTo(25.0);
    }
}
