package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.EntityNotFoundException;
import lab.hang.Gestion.boulangerie.exception.StockInsuffisantException;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.CompteBancaireRepository;
import lab.hang.Gestion.boulangerie.repository.LotRepository;
import lab.hang.Gestion.boulangerie.repository.StockMovementRepository;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import lab.hang.Gestion.boulangerie.service.MatierePremiereService;
import lab.hang.Gestion.boulangerie.service.StockService;
import lab.hang.Gestion.boulangerie.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private MatierePremiereService matierePremiereService;
    @Mock private LotRepository lotRepository;
    @Mock private UserService userService;
    @Mock private CompteBancaireRepository compteBancaireRepository;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks
    private StockService stockService;

    private MatierePremiere matierePremiere;
    private CompteBancaire compte;
    private User user;

    @BeforeEach
    void setUp() {
        matierePremiere = new MatierePremiere();
        matierePremiere.setId(1L);
        matierePremiere.setNom("Farine");
        matierePremiere.setStock(100.0);

        compte = new CompteBancaire();
        compte.setSolde(1000.0);

        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
    }

    @Test
    void addStock_devrait_decrementer_le_solde_du_compte() {
        // Given
        when(matierePremiereService.getMatierePremiereById(1L)).thenReturn(matierePremiere);
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(compte);
        when(userService.getCurrentUser()).thenReturn(user);
        when(stockMovementRepository.save(any())).thenReturn(new StockMovement());
        when(transactionRepository.save(any())).thenReturn(new Transaction());

        // When : achat de 10 unités à 50.0 = coût de 500
        stockService.addStock(1L, 10.0, 50.0);

        // Then : solde doit passer de 1000 à 500 (1000 - 500)
        assertThat(compte.getSolde()).isEqualTo(500.0);
        verify(compteBancaireRepository).save(compte);
    }

    @Test
    void addStock_avec_solde_insuffisant_doit_creer_achat_credit() {
        // Given : solde insuffisant
        compte.setSolde(100.0);
        when(matierePremiereService.getMatierePremiereById(1L)).thenReturn(matierePremiere);
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(compte);
        when(userService.getCurrentUser()).thenReturn(user);
        when(stockMovementRepository.save(any())).thenReturn(new StockMovement());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            // Vérifie que le type est ACHAT_CREDIT
            assertThat(t.getType()).isEqualTo("ACHAT_CREDIT");
            return t;
        });

        // When : achat de 10 à 50 = 500, solde = 100 (insuffisant)
        stockService.addStock(1L, 10.0, 50.0);

        // Then : solde négatif, type ACHAT_CREDIT
        assertThat(compte.getSolde()).isEqualTo(-400.0);
    }

    @Test
    void addStock_avec_matierePremiereId_null_doit_lever_IllegalArgumentException() {
        assertThatThrownBy(() -> stockService.addStock(null, 10.0, 50.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void addStock_avec_quantite_negative_doit_lever_IllegalArgumentException() {
        assertThatThrownBy(() -> stockService.addStock(1L, -5.0, 50.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positif");
    }

    @Test
    void addStock_avec_compte_bancaire_introuvable_doit_lever_EntityNotFoundException() {
        when(matierePremiereService.getMatierePremiereById(1L)).thenReturn(matierePremiere);
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(null);
        when(userService.getCurrentUser()).thenReturn(user);

        assertThatThrownBy(() -> stockService.addStock(1L, 10.0, 50.0))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void addStock_doit_incrementer_le_stock_de_la_matiere_premiere() {
        when(matierePremiereService.getMatierePremiereById(1L)).thenReturn(matierePremiere);
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(compte);
        when(userService.getCurrentUser()).thenReturn(user);
        when(stockMovementRepository.save(any())).thenReturn(new StockMovement());
        when(transactionRepository.save(any())).thenReturn(new Transaction());

        stockService.addStock(1L, 20.0, 10.0);

        assertThat(matierePremiere.getStock()).isEqualTo(120.0); // 100 + 20
    }

    @Test
    void removeStock_avec_stock_insuffisant_doit_lever_StockInsuffisantException() {
        matierePremiere.setStock(5.0);
        when(matierePremiereService.getMatierePremiereById(1L)).thenReturn(matierePremiere);
        when(userService.getCurrentUser()).thenReturn(user);

        assertThatThrownBy(() -> stockService.removeStock(1L, 10.0))
                .isInstanceOf(StockInsuffisantException.class);
    }

    @Test
    void removeStock_doit_decrementer_le_stock() {
        when(matierePremiereService.getMatierePremiereById(1L)).thenReturn(matierePremiere);
        when(userService.getCurrentUser()).thenReturn(user);
        when(stockMovementRepository.save(any())).thenReturn(new StockMovement());
        when(matierePremiereService.saveMatierePremiere(any())).thenReturn(matierePremiere);

        stockService.removeStock(1L, 30.0);

        assertThat(matierePremiere.getStock()).isEqualTo(70.0); // 100 - 30
    }
}
