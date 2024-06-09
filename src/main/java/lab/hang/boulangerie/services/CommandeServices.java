package lab.hang.boulangerie.services;

import lab.hang.boulangerie.dto.CommandeDTO;
import lab.hang.boulangerie.dto.LigneCommandeDTO;
import lab.hang.boulangerie.dto.MatiereDTO;
import lab.hang.boulangerie.dto.ProductDTO;
import lab.hang.boulangerie.entity.Commande;
import lab.hang.boulangerie.entity.LigneCommande;
import lab.hang.boulangerie.entity.Matiere;
import lab.hang.boulangerie.entity.Products;

import java.util.List;
import java.util.Map;

public interface CommandeServices {
    void SaveCommand(CommandeDTO commandeDTO , List<LigneCommandeDTO> ligneCommandeDTO);
    String DeleteCommande(Commande commande);
    CommandeDTO UpdateCommande(CommandeDTO commandeDTO , List<LigneCommandeDTO> ligneCommandeDTO);

    CommandeDTO getCommandeById(CommandeDTO commandeDTO);

    List<CommandeDTO> getAllCommande();

    Map<Matiere, Integer> getMatiereCommande();

    Map<Products, Integer> getProduitCommande();
}
