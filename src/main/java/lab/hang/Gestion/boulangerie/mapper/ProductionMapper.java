package lab.hang.Gestion.boulangerie.mapper;

import lab.hang.Gestion.boulangerie.dto.ProductionDTO;
import lab.hang.Gestion.boulangerie.model.Commande;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.Production;
import lab.hang.Gestion.boulangerie.model.Produit;
import lab.hang.Gestion.boulangerie.service.MatierePremiereService;
import lab.hang.Gestion.boulangerie.service.ProduitService;
import lab.hang.Gestion.boulangerie.service.UserService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductionMapper {

    private final MatierePremiereService matierePremiereService;
    private final ProduitService produitService;
    private final UserService userService;

    public ProductionMapper(MatierePremiereService matierePremiereService, ProduitService produitService, UserService userService) {
        this.matierePremiereService = matierePremiereService;
        this.produitService = produitService;
        this.userService = userService;
    }

    public Production toEntity(ProductionDTO productionDTO) {
        Production production = new Production();
        production.setDateProduction(productionDTO.getDateProduction());

        // Convertir les matières premières utilisées
        Map<MatierePremiere, Double> matieresPremieresUtilisees = new HashMap<>();
        if (productionDTO.getMatieresPremieresUtilisees() != null) {
            for (Map.Entry<Long, Double> entry : productionDTO.getMatieresPremieresUtilisees().entrySet()) {
                Long matiereId = entry.getKey();
                MatierePremiere matierePremiere = matierePremiereService.getMatierePremiereById(matiereId);
                matieresPremieresUtilisees.put(matierePremiere, entry.getValue());
            }
        }
        production.setMatieresPremieresUtilisees(matieresPremieresUtilisees);

        // Convertir les produits produits
        Map<Produit, Integer> produitsProduits = new HashMap<>();
        if (productionDTO.getProduitsProduits() != null) {
            for (Map.Entry<Long, Integer> entry : productionDTO.getProduitsProduits().entrySet()) {
                Long produitId = entry.getKey();
                Produit produit = produitService.getProduitEntityById(produitId);
                produitsProduits.put(produit, entry.getValue());
            }
        }
        production.setProduitsProduits(produitsProduits);

        // Convertir les quantités réelles utilisées
        Map<MatierePremiere, Double> quantitesReellesUtilisees = new HashMap<>();
        if (productionDTO.getQuantitesReellesUtilisees() != null) {
            for (Map.Entry<Long, Double> entry : productionDTO.getQuantitesReellesUtilisees().entrySet()) {
                Long matiereId = entry.getKey();
                MatierePremiere matierePremiere = matierePremiereService.getMatierePremiereById(matiereId);
                quantitesReellesUtilisees.put(matierePremiere, entry.getValue());
            }
        }
        production.setQuantitesReellesUtilisees(quantitesReellesUtilisees);

        production.setUser(userService.getUserById(productionDTO.getUserId()));

        return production;
    }

    public ProductionDTO toDTO(Production production) {
        ProductionDTO productionDTO = new ProductionDTO();
        productionDTO.setDateProduction(production.getDateProduction());
        productionDTO.setId(production.getId());

        // Convertir Map<MatierePremiere, Double> en Map<Long, Double>
        Map<Long, Double> matieresPremieresUtilisees = new HashMap<>();
        for (Map.Entry<MatierePremiere, Double> entry : production.getMatieresPremieresUtilisees().entrySet()) {
            matieresPremieresUtilisees.put(entry.getKey().getId(), entry.getValue());
        }
        productionDTO.setMatieresPremieresUtilisees(matieresPremieresUtilisees);

        // Convertir Map<Produit, Integer> en Map<Long, Integer>
        Map<Long, Integer> produitsProduits = new HashMap<>();
        for (Map.Entry<Produit, Integer> entry : production.getProduitsProduits().entrySet()) {
            produitsProduits.put(entry.getKey().getId(), entry.getValue());
        }
        productionDTO.setProduitsProduits(produitsProduits);

        // Convertir Map<MatierePremiere, Double> en Map<Long, Double>
        Map<Long, Double> quantitesReellesUtilisees = new HashMap<>();
        for (Map.Entry<MatierePremiere, Double> entry : production.getQuantitesReellesUtilisees().entrySet()) {
            quantitesReellesUtilisees.put(entry.getKey().getId(), entry.getValue());
        }
        productionDTO.setQuantitesReellesUtilisees(quantitesReellesUtilisees);

        // Convertir Map<Produit, Integer> en Map<Long, Integer>
        Map<Long, Integer> produitsRestants = new HashMap<>();
        if (production.getProduitsRestants() != null) {
            for (Map.Entry<Produit, Integer> entry : production.getProduitsRestants().entrySet()) {
                produitsRestants.put(entry.getKey().getId(), entry.getValue());
            }
        }
        productionDTO.setProduitsRestants(produitsRestants);

        productionDTO.setUserId(production.getUser().getId());
        // Mapper les commandes associées
        Set<Long> commandeIds = production.getCommandes()
                .stream()
                .map(Commande::getId)
                .collect(Collectors.toSet());
        productionDTO.setCommandeIds(commandeIds);

        return productionDTO;
    }
}