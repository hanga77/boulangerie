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
        when(venteLibreRepository.save(any())).thenAnswer(invocation -> {
            VenteLibre vl = invocation.getArgument(0);
            vl.setId(1L);
            return vl;
        });
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
        when(venteLibreRepository.save(any())).thenAnswer(invocation -> {
            VenteLibre vl = invocation.getArgument(0);
            vl.setId(1L);
            return vl;
        });
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(Optional.of(compte));
        when(venteLibreMapper.toDTO(any())).thenReturn(null);

        CreateVenteLibreRequest request = new CreateVenteLibreRequest();
        request.setProductionId(1L);
        request.setProduits(Map.of(1L, 2));
        // pas de guichetId ni moyenPaiement

        venteLibreService.createVenteLibre(request);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getDescription()).isEqualTo("Vente libre ID: 1");
    }
}
