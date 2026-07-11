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

import java.util.Optional;

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

    // ── addStock ───────────────────────────────────────────────────────────

    @Test
    void addStock_decremente_le_solde_du_compte() {
        when(matierePremiereService.getMatierePremiereById(1L)).thenReturn(matierePremiere);
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(Optional.of(compte));
        when(userService.getCurrentUser()).thenReturn(user);
        when(stockMovementRepository.save(any())).thenReturn(new StockMovement());
        when(transactionRepository.save(any())).thenReturn(new Transaction());

        stockService.addStock(1L, 10.0, 50.0, 10.0, 0.0); // 10 × 50 = 500

        assertThat(compte.getSolde()).isEqualTo(500.0); // 1000 - 500
        verify(compteBancaireRepository).save(compte);
    }

    @Test
    void addStock_incremente_le_stock_matiere_premiere() {
        when(matierePremiereService.getMatierePremiereById(1L)).thenReturn(matierePremiere);
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(Optional.of(compte));
        when(userService.getCurrentUser()).thenReturn(user);
        when(stockMovementRepository.save(any())).thenReturn(new StockMovement());
        when(transactionRepository.save(any())).thenReturn(new Transaction());

        stockService.addStock(1L, 20.0, 10.0, 20.0, 0.0);

        assertThat(matierePremiere.getStock()).isEqualTo(120.0); // 100 + 20
    }

    @Test
    void addStock_solde_insuffisant_cree_achat_a_credit() {
        compte.setSolde(100.0);
        when(matierePremiereService.getMatierePremiereById(1L)).thenReturn(matierePremiere);
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(Optional.of(compte));
        when(userService.getCurrentUser()).thenReturn(user);
        when(stockMovementRepository.save(any())).thenReturn(new StockMovement());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            assertThat(t.getType()).isEqualTo("ACHAT_CREDIT");
            return t;
        });

        stockService.addStock(1L, 10.0, 50.0, 10.0, 0.0); // coût 500 > solde 100

        assertThat(compte.getSolde()).isEqualTo(-400.0);
    }

    @Test
    void addStock_enregistre_mouvement_stock_entree() {
        when(matierePremiereService.getMatierePremiereById(1L)).thenReturn(matierePremiere);
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(Optional.of(compte));
        when(userService.getCurrentUser()).thenReturn(user);
        when(stockMovementRepository.save(any())).thenReturn(new StockMovement());
        when(transactionRepository.save(any())).thenReturn(new Transaction());

        stockService.addStock(1L, 15.0, 10.0, 15.0, 0.0);

        verify(stockMovementRepository).save(argThat(m ->
                "ENTREE".equals(m.getType()) && m.getQuantite() == 15.0));
    }

    @Test
    void addStock_matierePremiere_null_leve_IllegalArgumentException() {
        assertThatThrownBy(() -> stockService.addStock(null, 10.0, 50.0, 10.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void addStock_quantite_negative_leve_IllegalArgumentException() {
        assertThatThrownBy(() -> stockService.addStock(1L, -5.0, 50.0, -5.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positif");
    }

    @Test
    void addStock_compte_absent_leve_EntityNotFoundException() {
        when(matierePremiereService.getMatierePremiereById(1L)).thenReturn(matierePremiere);
        when(compteBancaireRepository.findByNom("Compte Principal")).thenReturn(Optional.empty());
        when(userService.getCurrentUser()).thenReturn(user);

        assertThatThrownBy(() -> stockService.addStock(1L, 10.0, 50.0, 10.0, 0.0))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Compte");
    }

    // ── removeStock ────────────────────────────────────────────────────────

    @Test
    void removeStock_decremente_le_stock() {
        when(matierePremiereService.getMatierePremiereByIdWithLock(1L)).thenReturn(matierePremiere);
        when(userService.getCurrentUser()).thenReturn(user);
        when(stockMovementRepository.save(any())).thenReturn(new StockMovement());
        when(matierePremiereService.saveMatierePremiere(any())).thenReturn(matierePremiere);

        stockService.removeStock(1L, 30.0);

        assertThat(matierePremiere.getStock()).isEqualTo(70.0); // 100 - 30
    }

    @Test
    void removeStock_avec_motif_decremente_le_stock() {
        when(matierePremiereService.getMatierePremiereByIdWithLock(1L)).thenReturn(matierePremiere);
        when(userService.getCurrentUser()).thenReturn(user);
        when(stockMovementRepository.save(any())).thenReturn(new StockMovement());
        when(matierePremiereService.saveMatierePremiere(any())).thenReturn(matierePremiere);

        stockService.removeStock(1L, 20.0, "PRODUCTION 2026-05-20");

        assertThat(matierePremiere.getStock()).isEqualTo(80.0);
        verify(stockMovementRepository).save(argThat(m ->
                "SORTIE".equals(m.getType()) &&
                m.getMotif().equals("PRODUCTION 2026-05-20")));
    }

    @Test
    void removeStock_stock_insuffisant_leve_StockInsuffisantException() {
        matierePremiere.setStock(5.0);
        when(matierePremiereService.getMatierePremiereByIdWithLock(1L)).thenReturn(matierePremiere);

        assertThatThrownBy(() -> stockService.removeStock(1L, 10.0))
                .isInstanceOf(StockInsuffisantException.class)
                .hasMessageContaining("Farine");
    }

    @Test
    void removeStock_quantite_zero_leve_IllegalArgumentException() {
        assertThatThrownBy(() -> stockService.removeStock(1L, 0.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void removeStock_enregistre_mouvement_stock_sortie() {
        when(matierePremiereService.getMatierePremiereByIdWithLock(1L)).thenReturn(matierePremiere);
        when(userService.getCurrentUser()).thenReturn(user);
        when(stockMovementRepository.save(any())).thenReturn(new StockMovement());
        when(matierePremiereService.saveMatierePremiere(any())).thenReturn(matierePremiere);

        stockService.removeStock(1L, 10.0);

        verify(stockMovementRepository).save(argThat(m -> "SORTIE".equals(m.getType())));
    }

    // ── returnStock ────────────────────────────────────────────────────────

    @Test
    void returnStock_incremente_le_stock() {
        when(matierePremiereService.getMatierePremiereById(1L)).thenReturn(matierePremiere);
        when(userService.getCurrentUser()).thenReturn(user);
        when(stockMovementRepository.save(any())).thenReturn(new StockMovement());
        when(matierePremiereService.saveMatierePremiere(any())).thenReturn(matierePremiere);

        stockService.returnStock(1L, 25.0, "RETOUR PRODUCTION");

        assertThat(matierePremiere.getStock()).isEqualTo(125.0); // 100 + 25
        verify(stockMovementRepository).save(argThat(m -> "RETOUR".equals(m.getType())));
    }

    @Test
    void returnStock_quantite_negative_leve_IllegalArgumentException() {
        assertThatThrownBy(() -> stockService.returnStock(1L, -1.0, "test"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
