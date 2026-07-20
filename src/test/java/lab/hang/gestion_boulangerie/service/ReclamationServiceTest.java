package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.PointDeVente;
import lab.hang.Gestion.boulangerie.model.Reclamation;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.PointDeVenteRepository;
import lab.hang.Gestion.boulangerie.repository.ReclamationRepository;
import lab.hang.Gestion.boulangerie.service.ReclamationService;
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
class ReclamationServiceTest {

    @Mock private ReclamationRepository reclamationRepository;
    @Mock private PointDeVenteRepository pointDeVenteRepository;

    @InjectMocks
    private ReclamationService reclamationService;

    private PointDeVente pointDeVente;
    private User auteur;

    @BeforeEach
    void setUp() {
        pointDeVente = new PointDeVente();
        pointDeVente.setId(1L);

        auteur = new User();
        auteur.setId(1L);
        auteur.setUsername("pdv-user");
    }

    @Test
    void creer_message_vide_leve_IllegalArgumentException() {
        assertThatThrownBy(() -> reclamationService.creer(1L, "  ", auteur))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(reclamationRepository);
    }

    @Test
    void creer_message_null_leve_IllegalArgumentException() {
        assertThatThrownBy(() -> reclamationService.creer(1L, null, auteur))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void creer_point_de_vente_introuvable_leve_ResourceNotFoundException() {
        when(pointDeVenteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reclamationService.creer(99L, "Produits manquants", auteur))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void creer_sauvegarde_avec_statut_ouverte() {
        when(pointDeVenteRepository.findById(1L)).thenReturn(Optional.of(pointDeVente));
        when(reclamationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reclamation result = reclamationService.creer(1L, "Produits manquants", auteur);

        assertThat(result.getStatut()).isEqualTo("OUVERTE");
        assertThat(result.getMessage()).isEqualTo("Produits manquants");
        assertThat(result.getPointDeVente()).isEqualTo(pointDeVente);
        assertThat(result.getAuteur()).isEqualTo(auteur);
        assertThat(result.getDateCreation()).isNotNull();
        assertThat(result.getReponse()).isNull();
    }

    @Test
    void repondre_reclamation_introuvable_leve_ResourceNotFoundException() {
        when(reclamationRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reclamationService.repondre(42L, "Réponse"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void repondre_met_a_jour_statut_et_reponse() {
        Reclamation existante = new Reclamation();
        existante.setId(1L);
        existante.setStatut("OUVERTE");
        when(reclamationRepository.findById(1L)).thenReturn(Optional.of(existante));
        when(reclamationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reclamation result = reclamationService.repondre(1L, "C'est corrigé.");

        assertThat(result.getStatut()).isEqualTo("REPONDUE");
        assertThat(result.getReponse()).isEqualTo("C'est corrigé.");
        assertThat(result.getDateReponse()).isNotNull();
    }

    @Test
    void getByPointDeVente_delegue_au_repository() {
        List<Reclamation> reclamations = List.of(new Reclamation());
        when(reclamationRepository.findByPointDeVenteIdOrderByDateCreationDesc(1L)).thenReturn(reclamations);

        assertThat(reclamationService.getByPointDeVente(1L)).isEqualTo(reclamations);
    }

    @Test
    void getAll_delegue_au_repository() {
        List<Reclamation> reclamations = List.of(new Reclamation());
        when(reclamationRepository.findAllByOrderByDateCreationDesc()).thenReturn(reclamations);

        assertThat(reclamationService.getAll()).isEqualTo(reclamations);
    }
}
