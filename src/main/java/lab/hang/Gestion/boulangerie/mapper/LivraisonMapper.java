package lab.hang.Gestion.boulangerie.mapper;

import lab.hang.Gestion.boulangerie.dto.LivraisonDTO;
import lab.hang.Gestion.boulangerie.dto.ProduitLivreDTO;
import lab.hang.Gestion.boulangerie.model.Livraison;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LivraisonMapper {
    public  LivraisonDTO toDTO(Livraison livraison) {
        LivraisonDTO dto = new LivraisonDTO();
        dto.setId(livraison.getId());
        dto.setDateLivraison(livraison.getDateLivraison());
        dto.setNomClient(livraison.getNomClient());
        dto.setProductionId(livraison.getProduction().getId());
        dto.setLivreurUsername(livraison.getUser().getUsername());

        Map<Long, ProduitLivreDTO> produitsDTO = new HashMap<>();
        livraison.getProduitsLivres().forEach((produit, produitLivre) -> {
            ProduitLivreDTO produitDTO = new ProduitLivreDTO();
            produitDTO.setProduitId(produit.getId());
            produitDTO.setNomProduit(produit.getNom());
            produitDTO.setQuantite(produitLivre.getQuantite());
            produitDTO.setPrixInitial(produitLivre.getPrixInitial());
            produitDTO.setPrixVente(produitLivre.getPrixVente());
            produitsDTO.put(produit.getId(), produitDTO);
        });
        dto.setProduitsLivres(produitsDTO);
        dto.setMontantTotal(livraison.getMontantTotal());
        return dto;
    }
}
