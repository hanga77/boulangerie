package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.exception.ProduitNotFoundException;
import lab.hang.Gestion.boulangerie.mapper.ProduitMapper;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.Produit;
import lab.hang.Gestion.boulangerie.repository.MatierePremiereRepository;
import lab.hang.Gestion.boulangerie.repository.ProduitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProduitService {

    private static final Logger log = LoggerFactory.getLogger(ProduitService.class);

    private final ProduitRepository produitRepository;
    private final ProduitMapper produitMapper;
    private final MatierePremiereRepository matierePremiereRepository;

    public ProduitService(ProduitRepository produitRepository, ProduitMapper produitMapper, MatierePremiereRepository matierePremiereRepository) {
        this.produitRepository = produitRepository;
        this.produitMapper = produitMapper;
        this.matierePremiereRepository = matierePremiereRepository;
    }

    @CacheEvict(value = "produits", allEntries = true)
    public Produit saveProduit(ProduitDTO produitDTO) {
        Produit produit = new Produit();
        produit.setNom(produitDTO.getNom());
        produit.setPrix(produitDTO.getPrix());
        produit.setQuantiteAttendue(produitDTO.getQuantiteAttendue());
        produit.setQuantiteVenteLibreJournaliere(produitDTO.getQuantiteVenteLibreJournaliere());

        log.debug("Sauvegarde produit: {}, matières premières: {}", produitDTO.getNom(), produitDTO.getMatieresPremieres());

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

    @CacheEvict(value = "produits", allEntries = true)
    public Produit updateProduit(Long id, ProduitDTO produitDTO) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException("Produit non trouvé avec l'ID : " + id));

        // Mettre à jour les champs de base
        produit.setNom(produitDTO.getNom());
        produit.setPrix(produitDTO.getPrix());
        produit.setQuantiteAttendue(produitDTO.getQuantiteAttendue());
        produit.setQuantiteVenteLibreJournaliere(produitDTO.getQuantiteVenteLibreJournaliere());

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

                log.debug("Matière ajoutée: {}, quantité base: {}, quantité calculée: {}",
                        matiere.getNom(), quantiteBase, quantiteNecessaire);
                matieresNecessaires.put(matiere, quantiteNecessaire);
            });
        } else {
            log.warn("Aucune matière première définie pour le produit: {}", produitDTO.getNom());
        }

        return matieresNecessaires;
    }

    public Page<ProduitDTO> getAllProduits(Pageable pageable) {
        return produitRepository.findAll(pageable)
                .map(produitMapper::toDTO);
    }

    @Cacheable("produits")
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

    @CacheEvict(value = "produits", allEntries = true)
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