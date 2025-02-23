package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.exception.ProduitNotFoundException;
import lab.hang.Gestion.boulangerie.mapper.ProduitMapper;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.Produit;
import lab.hang.Gestion.boulangerie.repository.MatierePremiereRepository;
import lab.hang.Gestion.boulangerie.repository.ProduitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final ProduitMapper produitMapper;
    private final MatierePremiereRepository matierePremiereRepository;

    public ProduitService(ProduitRepository produitRepository, ProduitMapper produitMapper, MatierePremiereRepository matierePremiereRepository) {
        this.produitRepository = produitRepository;
        this.produitMapper = produitMapper;
        this.matierePremiereRepository = matierePremiereRepository;
    }

    public Produit saveProduit(ProduitDTO produitDTO) {
        Produit produit = new Produit();
        produit.setNom(produitDTO.getNom());
        produit.setPrix(produitDTO.getPrix());
        produit.setQuantiteAttendue(produitDTO.getQuantiteAttendue());

        System.out.println("Saving product: " + produitDTO.getNom());
        System.out.println("Raw materials: " + produitDTO.getMatieresPremieres().entrySet());

        // Convertir la map de DTO en map d'entités MatierePremiere
        Map<MatierePremiere, Double> matieresPremieres = new HashMap<>();
        produitDTO.getMatieresPremieres().forEach((matiereId, quantite) -> {
            MatierePremiere matiere = matierePremiereRepository.findById(matiereId)
                    .orElseThrow(() -> new RuntimeException("Matière première non trouvée"));
            matieresPremieres.put(matiere, quantite);
        });
        produit.setMatieresPremieres(matieresPremieres);

        return produitRepository.save(produit);
    }

    public Produit updateProduit(Long id, ProduitDTO produitDTO) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException("Produit non trouvé avec l'ID : " + id));

        // Mettre à jour les champs de base
        produit.setNom(produitDTO.getNom());
        produit.setPrix(produitDTO.getPrix());
        produit.setQuantiteAttendue(produitDTO.getQuantiteAttendue());

        // Mettre à jour les matières premières nécessaires
        Map<MatierePremiere, Double> matieresPremieres = new HashMap<>();
        produitDTO.getMatieresPremieres().forEach((matiereId, quantite) -> {
            MatierePremiere matiere = matierePremiereRepository.findById(matiereId)
                    .orElseThrow(() -> new RuntimeException("Matière première non trouvée"));
            matieresPremieres.put(matiere, quantite);
        });
        produit.setMatieresPremieres(matieresPremieres);

        return produitRepository.save(produit);
    }

    public Map<MatierePremiere, Double> calculateMatieresPremieresNecessaires(ProduitDTO produitDTO, double quantiteCommandee) {
        Map<MatierePremiere, Double> matieresNecessaires = new HashMap<>();

        /*// Debug logging
        System.out.println("Calculating materials for product: " + produitDTO.getNom() +
                " (ID: " + produitDTO.getId() + "), quantity: " + quantiteCommandee);
        System.out.println("Raw materials in product: " + produitDTO.getMatieresPremieres());*/

        if (produitDTO.getMatieresPremieres() != null && !produitDTO.getMatieresPremieres().isEmpty()) {
            produitDTO.getMatieresPremieres().forEach((matiereId, quantiteBase) -> {
                MatierePremiere matiere = matierePremiereRepository.findById(matiereId)
                        .orElseThrow(() -> new RuntimeException("Matière première non trouvée avec l'ID : " + matiereId));

                // Calculate required quantity based on ordered quantity
                double quantiteNecessaire = quantiteBase * quantiteCommandee;

                // Debug logging
                System.out.println("Adding material: " + matiere.getNom() +
                        ", base quantity: " + quantiteBase +
                        ", calculated quantity: " + quantiteNecessaire);

                matieresNecessaires.put(matiere, quantiteNecessaire);
            });
        } else {
            System.out.println("Warning: No raw materials defined for product " + produitDTO.getNom());
        }

        return matieresNecessaires;
    }

    public Page<ProduitDTO> getAllProduits(Pageable pageable) {
        return produitRepository.findAll(pageable)
                .map(produitMapper::toDTO);
    }

    public List<ProduitDTO> getAllProduits() {
        return produitRepository.findAll().stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ProduitDTO getProduitById(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException("Produit non trouvé avec l'ID : " + id));
        return produitMapper.toDTO(produit);
    }

    public Produit getProduitEntityById(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException("Produit non trouvé avec l'ID : " + id));
    }

    public void deleteProduit(Long id) {
        if (!produitRepository.existsById(id)) {
            throw new ProduitNotFoundException("Produit non trouvé avec l'ID : " + id);
        }
        produitRepository.deleteById(id);
    }

    public List<ProduitDTO> searchProduits(String nom) {
        return produitRepository.findByNomContainingIgnoreCase(nom).stream()
                .map(produitMapper::toDTO) // Utiliser produitMapper::toDTO
                .collect(Collectors.toList());
    }


}