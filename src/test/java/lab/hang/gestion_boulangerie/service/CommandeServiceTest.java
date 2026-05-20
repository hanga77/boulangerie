package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.CommandeDTO;
import lab.hang.Gestion.boulangerie.exception.CommandeNotFoundException;
import lab.hang.Gestion.boulangerie.mapper.CommandeMapper;
import lab.hang.Gestion.boulangerie.model.Commande;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.Produit;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.CommandeRepository;
import lab.hang.Gestion.boulangerie.service.CommandeService;
import lab.hang.Gestion.boulangerie.service.PointDeVenteService;
import lab.hang.Gestion.boulangerie.service.ProduitService;
import lab.hang.Gestion.boulangerie.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandeServiceTest {

    @Mock private CommandeRepository commandeRepository;
    @Mock private CommandeMapper commandeMapper;
    @Mock private ProduitService produitService;
    @Mock private UserService userService;
    @Mock private PointDeVenteService pointDeVenteService;

    @InjectMocks
    private CommandeService commandeService;

    // ── getCommandesNonTraitees ────────────────────────────────────────────

    @Test
    void getCommandesNonTraitees_liste_vide_ne_leve_pas_exception() {
        when(commandeRepository.findByProcessedFalse()).thenReturn(List.of());

        List<CommandeDTO> result = commandeService.getCommandesNonTraitees();

        assertThat(result).isEmpty();
    }

    @Test
    void getCommandesNonTraitees_retourne_seulement_commandes_non_traitees() {
        Commande c1 = new Commande();
        CommandeDTO dto1 = new CommandeDTO();
        dto1.setProcessed(false);

        when(commandeRepository.findByProcessedFalse()).thenReturn(List.of(c1));
        when(commandeMapper.toDTO(c1)).thenReturn(dto1);

        List<CommandeDTO> result = commandeService.getCommandesNonTraitees();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isProcessed()).isFalse();
    }

    // ── getCommandeById ────────────────────────────────────────────────────

    @Test
    void getCommandeById_id_inexistant_leve_CommandeNotFoundException() {
        when(commandeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandeService.getCommandeById(99L))
                .isInstanceOf(CommandeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getCommandeById_id_valide_retourne_dto() {
        Commande commande = new Commande();
        commande.setId(1L);
        CommandeDTO dto = new CommandeDTO();
        dto.setId(1L);

        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(commandeMapper.toDTO(commande)).thenReturn(dto);

        CommandeDTO result = commandeService.getCommandeById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    // ── createCommande ─────────────────────────────────────────────────────

    @Test
    void createCommande_affecte_utilisateur_courant() {
        User user = new User();
        user.setId(1L);
        user.setUsername("boulanger");

        CommandeDTO dto = new CommandeDTO();
        dto.setDateCommande(LocalDate.now());
        dto.setNomPointDeVente("Marché central");
        dto.setProduitsCommandes(Map.of(1L, 5));

        Commande commande = new Commande();
        Commande saved = new Commande();
        saved.setId(1L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(commandeMapper.toEntity(dto)).thenReturn(commande);
        when(commandeRepository.save(commande)).thenReturn(saved);
        when(commandeMapper.toDTO(saved)).thenReturn(dto);

        CommandeDTO result = commandeService.createCommande(dto);

        assertThat(result).isNotNull();
        assertThat(commande.getUser()).isEqualTo(user);
        verify(commandeRepository).save(commande);
    }

    @Test
    void createCommande_persiste_la_commande() {
        User user = new User();
        user.setId(1L);

        CommandeDTO dto = new CommandeDTO();
        dto.setDateCommande(LocalDate.now());
        Commande commande = new Commande();
        Commande saved = new Commande();
        saved.setId(42L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(commandeMapper.toEntity(dto)).thenReturn(commande);
        when(commandeRepository.save(commande)).thenReturn(saved);
        when(commandeMapper.toDTO(saved)).thenReturn(dto);

        commandeService.createCommande(dto);

        verify(commandeRepository, times(1)).save(commande);
    }

    // ── deleteCommande ─────────────────────────────────────────────────────

    @Test
    void deleteCommande_id_inexistant_leve_CommandeNotFoundException() {
        when(commandeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> commandeService.deleteCommande(99L))
                .isInstanceOf(CommandeNotFoundException.class);
    }

    @Test
    void deleteCommande_id_valide_supprime_la_commande() {
        when(commandeRepository.existsById(1L)).thenReturn(true);

        commandeService.deleteCommande(1L);

        verify(commandeRepository).deleteById(1L);
    }

    // ── calculerMatieresPremieresPourDate ──────────────────────────────────

    @Test
    void calculerMatieres_sans_commande_retourne_map_vide() {
        when(commandeRepository.findByDateCommandeAndProcessed(any(), eq(false)))
                .thenReturn(List.of());

        Map<MatierePremiere, Double> result =
                commandeService.calculerMatieresPremieresPourDate(LocalDate.now());

        assertThat(result).isEmpty();
    }

    @Test
    void calculerMatieres_agregge_correctement_plusieurs_commandes() {
        MatierePremiere farine = new MatierePremiere();
        farine.setId(10L);
        farine.setNom("Farine");

        Produit pain = new Produit();
        pain.setId(1L);

        // Commande 1 : 5 pains
        Commande c1 = new Commande();
        c1.setProduitsCommandes(Map.of(pain, 5));

        // Commande 2 : 3 pains
        Commande c2 = new Commande();
        c2.setProduitsCommandes(Map.of(pain, 3));

        lab.hang.Gestion.boulangerie.dto.ProduitDTO painDTO = new lab.hang.Gestion.boulangerie.dto.ProduitDTO();
        painDTO.setId(1L);

        when(commandeRepository.findByDateCommandeAndProcessed(any(), eq(false)))
                .thenReturn(List.of(c1, c2));
        when(produitService.getProduitById(1L)).thenReturn(painDTO);
        when(produitService.calculateMatieresPremieresNecessaires(painDTO, 5))
                .thenReturn(Map.of(farine, 2.5));
        when(produitService.calculateMatieresPremieresNecessaires(painDTO, 3))
                .thenReturn(Map.of(farine, 1.5));

        Map<MatierePremiere, Double> result =
                commandeService.calculerMatieresPremieresPourDate(LocalDate.now());

        // 2.5 + 1.5 = 4.0
        assertThat(result).containsEntry(farine, 4.0);
    }

    // ── hasCommandeForToday ────────────────────────────────────────────────

    @Test
    void hasCommandeForToday_retourne_false_quand_aucune_commande() {
        when(commandeRepository.countByDateCommande(any())).thenReturn(0);

        assertThat(commandeService.hasCommandeForToday()).isFalse();
    }

    @Test
    void hasCommandeForToday_retourne_true_quand_commande_existe() {
        when(commandeRepository.countByDateCommande(any())).thenReturn(3);

        assertThat(commandeService.hasCommandeForToday()).isTrue();
    }
}
