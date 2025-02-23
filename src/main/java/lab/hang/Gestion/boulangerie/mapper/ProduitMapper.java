package lab.hang.Gestion.boulangerie.mapper;

import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.Produit;
import lab.hang.Gestion.boulangerie.repository.MatierePremiereRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProduitMapper {

    private final MatierePremiereRepository matierePremiereRepository;

    public ProduitMapper(MatierePremiereRepository matierePremiereRepository) {
        this.matierePremiereRepository = matierePremiereRepository;
    }

    // Convertir ProduitDTO en Produit
    public Produit toEntity(ProduitDTO produitDTO) {
        Produit produit = new Produit();
        produit.setId(produitDTO.getId());
        produit.setNom(produitDTO.getNom());
        produit.setPrix(produitDTO.getPrix());
        produit.setQuantiteAttendue(produitDTO.getQuantiteAttendue());

        // Convertir Map<Long, Double> en Map<MatierePremiere, Double>
        Map<MatierePremiere, Double> matieresPremieres = new HashMap<>();
        produitDTO.getMatieresPremieres().forEach((matiereId, quantite) -> {
            MatierePremiere matierePremiere = matierePremiereRepository.findById(matiereId)
                    .orElseThrow(() -> new RuntimeException("Matière première non trouvée avec l'ID : " + matiereId));
            matieresPremieres.put(matierePremiere, quantite);
        });
        produit.setMatieresPremieres(matieresPremieres);

        return produit;
    }

    // Convertir Produit en ProduitDTO
    public ProduitDTO toDTO(Produit produit) {
        ProduitDTO produitDTO = new ProduitDTO();
        produitDTO.setId(produit.getId());
        produitDTO.setNom(produit.getNom());
        produitDTO.setPrix(produit.getPrix());
        produitDTO.setQuantiteAttendue(produit.getQuantiteAttendue());

        // Convertir Map<MatierePremiere, Double> en Map<Long, Double>
        Map<Long, Double> matieresPremieres = new HashMap<>();
        produit.getMatieresPremieres().forEach((matierePremiere, quantite) -> {
            matieresPremieres.put(matierePremiere.getId(), quantite);
        });
        produitDTO.setMatieresPremieres(matieresPremieres);

        return produitDTO;
    }
}