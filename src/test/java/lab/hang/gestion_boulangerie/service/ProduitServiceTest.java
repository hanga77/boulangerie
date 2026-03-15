package lab.hang.gestion_boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.exception.ProduitNotFoundException;
import lab.hang.Gestion.boulangerie.mapper.ProduitMapper;
import lab.hang.Gestion.boulangerie.model.Produit;
import lab.hang.Gestion.boulangerie.repository.MatierePremiereRepository;
import lab.hang.Gestion.boulangerie.repository.ProduitRepository;
import lab.hang.Gestion.boulangerie.service.ProduitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProduitServiceTest {

    @Mock private ProduitRepository produitRepository;
    @Mock private ProduitMapper produitMapper;
    @Mock private MatierePremiereRepository matierePremiereRepository;

    @InjectMocks
    private ProduitService produitService;

    @Test
    void getProduitById_avec_id_inexistant_doit_lever_ProduitNotFoundException() {
        when(produitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produitService.getProduitById(99L))
                .isInstanceOf(ProduitNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getProduitById_doit_retourner_le_dto() {
        Produit produit = new Produit();
        produit.setId(1L);
        produit.setNom("Baguette");

        ProduitDTO dto = new ProduitDTO();
        dto.setId(1L);
        dto.setNom("Baguette");

        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
        when(produitMapper.toDTO(produit)).thenReturn(dto);

        ProduitDTO result = produitService.getProduitById(1L);

        assertThat(result.getNom()).isEqualTo("Baguette");
    }

    @Test
    void deleteProduit_avec_id_inexistant_doit_lever_ProduitNotFoundException() {
        when(produitRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> produitService.deleteProduit(99L))
                .isInstanceOf(ProduitNotFoundException.class);
    }

    @Test
    void deleteProduit_doit_appeler_deleteById() {
        when(produitRepository.existsById(1L)).thenReturn(true);

        produitService.deleteProduit(1L);

        verify(produitRepository).deleteById(1L);
    }

    @Test
    void getAllProduits_doit_retourner_liste_vide_si_aucun_produit() {
        when(produitRepository.findAll()).thenReturn(List.of());

        List<ProduitDTO> result = produitService.getAllProduits();

        assertThat(result).isEmpty();
    }

    @Test
    void calculateMatieresPremieresNecessaires_sans_matieres_doit_retourner_map_vide() {
        ProduitDTO dto = new ProduitDTO();
        dto.setNom("Baguette");
        dto.setMatieresPremieres(new HashMap<>());

        var result = produitService.calculateMatieresPremieresNecessaires(dto, 5.0);

        assertThat(result).isEmpty();
    }
}
