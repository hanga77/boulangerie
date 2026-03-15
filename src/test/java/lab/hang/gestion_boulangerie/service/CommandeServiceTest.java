package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.CommandeDTO;
import lab.hang.Gestion.boulangerie.exception.CommandeNotFoundException;
import lab.hang.Gestion.boulangerie.mapper.CommandeMapper;
import lab.hang.Gestion.boulangerie.model.Commande;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.CommandeRepository;
import lab.hang.Gestion.boulangerie.service.CommandeService;
import lab.hang.Gestion.boulangerie.service.ProduitService;
import lab.hang.Gestion.boulangerie.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
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

    @InjectMocks
    private CommandeService commandeService;

    @Test
    void getCommandesNonTraitees_liste_vide_ne_doit_pas_lever_exception() {
        when(commandeRepository.findByProcessedFalse()).thenReturn(List.of());
        when(commandeMapper.toDTO(any())).thenReturn(new CommandeDTO());

        List<CommandeDTO> result = commandeService.getCommandesNonTraitees();

        assertThat(result).isEmpty();
    }

    @Test
    void getCommandeById_avec_id_inexistant_doit_lever_CommandeNotFoundException() {
        when(commandeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandeService.getCommandeById(99L))
                .isInstanceOf(CommandeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createCommande_doit_affecter_utilisateur_courant() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        CommandeDTO dto = new CommandeDTO();
        dto.setDateCommande(LocalDate.now());
        dto.setPointDeVente("Paris");

        Commande commande = new Commande();
        Commande savedCommande = new Commande();
        savedCommande.setId(1L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(commandeMapper.toEntity(dto)).thenReturn(commande);
        when(commandeRepository.save(commande)).thenReturn(savedCommande);
        when(commandeMapper.toDTO(savedCommande)).thenReturn(dto);

        CommandeDTO result = commandeService.createCommande(dto);

        assertThat(result).isNotNull();
        verify(commandeRepository).save(commande);
        assertThat(commande.getUser()).isEqualTo(user);
    }

    @Test
    void deleteCommande_avec_id_inexistant_doit_lever_CommandeNotFoundException() {
        when(commandeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandeService.deleteCommande(99L))
                .isInstanceOf(CommandeNotFoundException.class);
    }
}
