package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.PointDeVente;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.model.Versement;
import lab.hang.Gestion.boulangerie.repository.LivraisonRepository;
import lab.hang.Gestion.boulangerie.repository.PointDeVenteRepository;
import lab.hang.Gestion.boulangerie.repository.VersementRepository;
import lab.hang.Gestion.boulangerie.service.VersementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VersementServiceTest {

    @Mock private VersementRepository versementRepository;
    @Mock private LivraisonRepository livraisonRepository;
    @Mock private PointDeVenteRepository pointDeVenteRepository;

    @InjectMocks
    private VersementService versementService;

    private PointDeVente pointDeVente;
    private User user;

    @BeforeEach
    void setUp() {
        pointDeVente = new PointDeVente();
        pointDeVente.setId(1L);
        pointDeVente.setNom("Boutique Centrale");

        user = new User();
        user.setId(1L);
        user.setUsername("admin");
    }

    // ── calculerSolde ──────────────────────────────────────────────────────

    @Test
    void calculerSolde_est_total_livre_moins_total_verse() {
        when(livraisonRepository.sumMontantByPointDeVenteId(1L)).thenReturn(5000.0);
        when(versementRepository.sumMontantByPointDeVenteId(1L)).thenReturn(2000.0);

        double solde = versementService.calculerSolde(1L);

        assertThat(solde).isEqualTo(3000.0);
    }

    @Test
    void calculerSolde_zero_quand_tout_est_verse() {
        when(livraisonRepository.sumMontantByPointDeVenteId(1L)).thenReturn(5000.0);
        when(versementRepository.sumMontantByPointDeVenteId(1L)).thenReturn(5000.0);

        assertThat(versementService.calculerSolde(1L)).isEqualTo(0.0);
    }

    // ── enregistrerVersement ───────────────────────────────────────────────

    @Test
    void enregistrerVersement_montant_negatif_leve_IllegalArgumentException() {
        assertThatThrownBy(() -> versementService.enregistrerVersement(1L, -100.0, "test", user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positif");
    }

    @Test
    void enregistrerVersement_montant_zero_leve_IllegalArgumentException() {
        assertThatThrownBy(() -> versementService.enregistrerVersement(1L, 0.0, "test", user))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void enregistrerVersement_point_de_vente_introuvable_leve_ResourceNotFoundException() {
        when(pointDeVenteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> versementService.enregistrerVersement(99L, 500.0, "test", user))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void enregistrerVersement_sauvegarde_avec_les_bonnes_valeurs() {
        when(pointDeVenteRepository.findById(1L)).thenReturn(Optional.of(pointDeVente));
        when(versementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Versement result = versementService.enregistrerVersement(1L, 800.0, "Versement partiel", user);

        assertThat(result.getMontant()).isEqualTo(800.0);
        assertThat(result.getMotif()).isEqualTo("Versement partiel");
        assertThat(result.getPointDeVente()).isEqualTo(pointDeVente);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getDate()).isEqualTo(java.time.LocalDate.now());
    }

    @Test
    void getVersementsByPointDeVente_delegue_au_repository() {
        List<Versement> versements = List.of(new Versement());
        when(versementRepository.findByPointDeVenteIdOrderByDateDesc(1L)).thenReturn(versements);

        assertThat(versementService.getVersementsByPointDeVente(1L)).isEqualTo(versements);
    }
}
