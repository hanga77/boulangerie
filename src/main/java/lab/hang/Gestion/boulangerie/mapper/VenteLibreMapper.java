package lab.hang.Gestion.boulangerie.mapper;

import lab.hang.Gestion.boulangerie.dto.VenteLibreDTO;
import lab.hang.Gestion.boulangerie.model.VenteLibre;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class VenteLibreMapper {

    public VenteLibreDTO toDTO(VenteLibre venteLibre) {
        VenteLibreDTO dto = new VenteLibreDTO();
        dto.setId(venteLibre.getId());
        dto.setDateVente(venteLibre.getDateVente());
        dto.setProductionId(venteLibre.getProduction().getId());
        dto.setVendeurUsername(venteLibre.getUser().getUsername());
        dto.setMontantTotal(venteLibre.getMontantTotal());
        if (venteLibre.getGuichet() != null) {
            dto.setGuichetId(venteLibre.getGuichet().getId());
            dto.setNomGuichet(venteLibre.getGuichet().getNom());
        }

        Map<Long, Integer> produitsVendus = new HashMap<>();
        Map<Long, String> nomsProduits = new HashMap<>();
        Map<Long, Double> prixProduits = new HashMap<>();

        venteLibre.getProduitsVendus().forEach((produit, quantite) -> {
            produitsVendus.put(produit.getId(), quantite);
            nomsProduits.put(produit.getId(), produit.getNom());
            prixProduits.put(produit.getId(), produit.getPrix());
        });

        dto.setProduitsVendus(produitsVendus);
        dto.setNomsProduits(nomsProduits);
        dto.setPrixProduits(prixProduits);

        return dto;
    }
}
