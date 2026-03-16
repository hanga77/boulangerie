package lab.hang.Gestion.boulangerie.mapper;

import lab.hang.Gestion.boulangerie.dto.CommandeDTO;
import lab.hang.Gestion.boulangerie.model.Commande;
import lab.hang.Gestion.boulangerie.model.Produit;
import lab.hang.Gestion.boulangerie.service.PointDeVenteService;
import lab.hang.Gestion.boulangerie.service.ProduitService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommandeMapper {

    private final ProduitService produitService;
    private final PointDeVenteService pointDeVenteService;

    public CommandeMapper(ProduitService produitService, PointDeVenteService pointDeVenteService) {
        this.produitService = produitService;
        this.pointDeVenteService = pointDeVenteService;
    }
    // Convertir CommandeDTO en Commande
    public Commande toEntity(CommandeDTO commandeDTO) {
        Commande commande = new Commande();
        if (commandeDTO.getId()!=null){
            commande.setId(commandeDTO.getId());
        }
        commande.setDateCommande(commandeDTO.getDateCommande());
        if (commandeDTO.getPointDeVenteId() != null) {
            commande.setPointDeVente(pointDeVenteService.getEntityById(commandeDTO.getPointDeVenteId()));
        }
        if (commandeDTO.getGuichetId() != null) {
            commande.setGuichet(pointDeVenteService.getGuichetEntityById(commandeDTO.getGuichetId()));
        }

        commande.setProcessed(commandeDTO.isProcessed());
        // Convertir Map<Long, Integer> en Map<Produit, Integer>
        Map<Produit, Integer> produitsCommandes = new HashMap<>();
        final double[] coutTotal = {0.0};

        commandeDTO.getProduitsCommandes().forEach((produitId, quantite) -> {
            Produit produit = produitService.getProduitEntityById(produitId);
            produitsCommandes.put(produit, quantite);
            coutTotal[0] += produit.getPrix() * quantite;
        });

        commande.setProduitsCommandes(produitsCommandes);
        commande.setCoutTotal(coutTotal[0]);

        return commande;
    }

    // Convertir Commande en CommandeDTO
    public CommandeDTO toDTO(Commande commande) {
        CommandeDTO commandeDTO = new CommandeDTO();
        commandeDTO.setId(commande.getId());
        commandeDTO.setDateCommande(commande.getDateCommande());
        if (commande.getPointDeVente() != null) {
            commandeDTO.setPointDeVenteId(commande.getPointDeVente().getId());
            commandeDTO.setNomPointDeVente(commande.getPointDeVente().getNom());
        }
        if (commande.getGuichet() != null) {
            commandeDTO.setGuichetId(commande.getGuichet().getId());
            commandeDTO.setNomGuichet(commande.getGuichet().getNom());
        }

        commandeDTO.setProcessed(commande.isProcessed());
        // Convertir Map<Produit, Integer> en Map<Long, Integer>
        Map<Long, Integer> produitsCommandes = new HashMap<>();
        commande.getProduitsCommandes().forEach((produit, quantite) -> {
            produitsCommandes.put(produit.getId(), quantite);
        });
        commandeDTO.setProduitsCommandes(produitsCommandes);

        // Mapper la relation avec Production
        if (commande.getProduction() != null) {
            commandeDTO.setProductionId(commande.getProduction().getId());
        }

        commandeDTO.setCoutTotal(commande.getCoutTotal());
        commandeDTO.setUserId(commande.getUser().getId());
        return commandeDTO;
    }
}