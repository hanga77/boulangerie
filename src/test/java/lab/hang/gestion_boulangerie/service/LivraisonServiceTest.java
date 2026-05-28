package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.CreateLivraisonRequest;
import lab.hang.Gestion.boulangerie.dto.LivraisonDTO;
import lab.hang.Gestion.boulangerie.dto.ProduitLivreRequest;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.mapper.LivraisonMapper;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.*;
import lab.hang.Gestion.boulangerie.service.FacturationService;
import lab.hang.Gestion.boulangerie.service.LivraisonService;
import lab.hang.Gestion.boulangerie.service.ProductionService;
import lab.hang.Gestion.boulangerie.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivraisonServiceTest {

    @Mock private LivraisonRepository livraisonRepository;
    @Mock private ProductionRepository productionRepository;
    @Mock private ProduitRepository produitRepository;
    @Mock private UserService userService;
    @Mock private ProductionService productionService;
    @Mock private LivraisonMapper livraisonMapper;
    @Mock private TransactionRepository transactionRepository;
    @Mock private CompteBancaireRepository compteBancaireRepository;
    @Mock private FacturationService facturationService;

    @InjectMocks
    private LivraisonService livraisonService;

    private User user;
    private Produit pain;
    private Production production;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("livreur");

        pain = new Produit();
        pain.setId(1L);
        pain.setNom("Pain");
        pain.setPrix(200.0);

        production = new Production();
        production.setId(1L);
        production.setDateProduction(LocalDate.now());
        Map<Produit, Integer> restants = new HashMap<>();
        restants.put(pain, 50);
        production.setProduitsRestants(restants);
    }

    // ── createLivraison ────────────────────────────────────────────────────

    @Test
    void createLivraison_succes_deduit_stock_produit_et_calcule_montant() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(productionRepository.findById(1L)).thenReturn(Optional.of(production));
        when(produitRepository.findById(1L)).thenReturn(Optional.of(pain));

        Livraison savedLivraison = new Livraison();
        savedLivraison.setId(10L);
        savedLivraison.setDateLivraison(LocalDate.now());
        when(livraisonRepository.save(any())).thenReturn(savedLivraison);
        when(livraisonMapper.toDTO(savedLivraison)).thenReturn(new LivraisonDTO());

        CreateLivraisonRequest request = buildRequest(1L, 10, 250.0);

        LivraisonDTO result = livraisonService.createLivraison(request);

        assertThat(result).isNotNull();
        // Stock restant doit passer de 50 à 40
        assertThat(production.getProduitsRestants().get(pain)).isEqualTo(40);
        verify(livraisonRepository).save(any());
        verify(productionRepository).save(production);
    }

    @Test
    void createLivraison_montant_total_est_prix_vente_x_quantite() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(productionRepository.findById(1L)).thenReturn(Optional.of(production));
        when(produitRepository.findById(1L)).thenReturn(Optional.of(pain));

        Livraison savedLivraison = new Livraison();
        savedLivraison.setId(1L);

        when(livraisonRepository.save(any())).thenAnswer(inv -> {
            Livraison l = inv.getArgument(0);
            // Vérifier le montant total : 5 unités × 300 XAF = 1500 XAF
            assertThat(l.getMontantTotal()).isEqualTo(1500.0);
            l.setId(1L);
            return l;
        });
        when(livraisonMapper.toDTO(any())).thenReturn(new LivraisonDTO());

        livraisonService.createLivraison(buildRequest(1L, 5, 300.0));
    }

    @Test
    void createLivraison_stock_insuffisant_leve_IllegalArgumentException() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(productionRepository.findById(1L)).thenReturn(Optional.of(production));
        when(produitRepository.findById(1L)).thenReturn(Optional.of(pain));

        // Demander 100 alors qu'il n'en reste que 50
        CreateLivraisonRequest request = buildRequest(1L, 100, 200.0);

        assertThatThrownBy(() -> livraisonService.createLivraison(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pain")
                .hasMessageContaining("50")
                .hasMessageContaining("100");
    }

    @Test
    void createLivraison_production_avant_hier_leve_IllegalArgumentException() {
        production.setDateProduction(LocalDate.now().minusDays(2));
        when(userService.getCurrentUser()).thenReturn(user);
        when(productionRepository.findById(1L)).thenReturn(Optional.of(production));

        assertThatThrownBy(() -> livraisonService.createLivraison(buildRequest(1L, 5, 200.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("veille");
    }

    @Test
    void createLivraison_production_de_la_veille_est_autorisee() {
        production.setDateProduction(LocalDate.now().minusDays(1));
        when(userService.getCurrentUser()).thenReturn(user);
        when(productionRepository.findById(1L)).thenReturn(Optional.of(production));
        when(produitRepository.findById(1L)).thenReturn(Optional.of(pain));

        Livraison saved = new Livraison();
        saved.setId(1L);
        saved.setDateLivraison(LocalDate.now().minusDays(1));
        when(livraisonRepository.save(any())).thenReturn(saved);
        when(livraisonMapper.toDTO(saved)).thenReturn(new LivraisonDTO());

        assertThatNoException().isThrownBy(() ->
                livraisonService.createLivraison(buildRequest(1L, 5, 200.0)));
    }

    @Test
    void createLivraison_production_introuvable_leve_ResourceNotFoundException() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(productionRepository.findById(99L)).thenReturn(Optional.empty());

        CreateLivraisonRequest request = buildRequest(1L, 5, 200.0);
        request.setProductionId(99L);

        assertThatThrownBy(() -> livraisonService.createLivraison(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getLivraisonById ───────────────────────────────────────────────────

    @Test
    void getLivraisonById_id_inexistant_leve_ResourceNotFoundException() {
        when(livraisonRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> livraisonService.getLivraisonById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Livraison");
    }

    // ── enregistrerRevenuLivraison ─────────────────────────────────────────

    @Test
    void enregistrerRevenuLivraison_augmente_solde_et_cree_transaction() {
        Livraison livraison = new Livraison();
        livraison.setRevenuEnregistre(false);
        when(livraisonRepository.findById(1L)).thenReturn(Optional.of(livraison));

        CompteBancaire compte = new CompteBancaire();
        compte.setSolde(5000.0);
        when(compteBancaireRepository.findByNom("Compte Principal"))
                .thenReturn(Optional.of(compte));
        when(transactionRepository.save(any())).thenReturn(new Transaction());

        livraisonService.enregistrerRevenuLivraison(1L, 1500.0);

        assertThat(compte.getSolde()).isEqualTo(6500.0);
        verify(compteBancaireRepository).save(compte);
        verify(transactionRepository).save(argThat(t ->
                "VENTE".equals(t.getType()) && t.getMontant() == 1500.0));
    }

    @Test
    void enregistrerRevenuLivraison_compte_absent_leve_ResourceNotFoundException() {
        Livraison livraison = new Livraison();
        livraison.setRevenuEnregistre(false);
        when(livraisonRepository.findById(1L)).thenReturn(Optional.of(livraison));
        when(compteBancaireRepository.findByNom("Compte Principal"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> livraisonService.enregistrerRevenuLivraison(1L, 500.0))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Compte");
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private CreateLivraisonRequest buildRequest(Long produitId, int quantite, double prixVente) {
        ProduitLivreRequest plr = new ProduitLivreRequest();
        plr.setQuantite(quantite);
        plr.setPrixVente(prixVente);

        CreateLivraisonRequest request = new CreateLivraisonRequest();
        request.setProductionId(1L);
        request.setNomClient("Client Test");
        request.setProduits(Map.of(produitId, plr));
        return request;
    }
}
