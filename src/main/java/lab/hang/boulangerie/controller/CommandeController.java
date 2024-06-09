package lab.hang.boulangerie.controller;


import jakarta.validation.Valid;
import lab.hang.boulangerie.dto.CommandeDTO;
import lab.hang.boulangerie.dto.LigneCommandeDTO;
import lab.hang.boulangerie.dto.PointVenteDTO;
import lab.hang.boulangerie.dto.ProductDTO;
import lab.hang.boulangerie.entity.Products;
import lab.hang.boulangerie.services.CommandeServices;
import lab.hang.boulangerie.services.PointDeVenteServices;
import lab.hang.boulangerie.services.ProductsServices;
import lab.hang.boulangerie.services.SessionServices;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/commande")
public class CommandeController {
    private  final CommandeServices commandeServices;
    private final ProductsServices productsServices;
    private  final SessionServices sessionServices;
    private final PointDeVenteServices pointDeVenteServices;

    @PostMapping("/save")
    public String ajouterCommande(@Valid @ModelAttribute("commande") CommandeDTO commande,
                                  @Valid @ModelAttribute("produits") List<String> produits,
                                  @Valid @ModelAttribute("quantites") List<Integer> quantites) {

        List<LigneCommandeDTO> ligneCommandes = new ArrayList<>();
        double prix = 0;
        commande.setSession(sessionServices.getLastSessions());
        for (int i = 0; i < produits.size(); i++) {
            Products products = productsServices.getProducts(produits.get(i));
            if(quantites.get(i)!=0){
                prix = products.getPrix()*quantites.get(i) + prix;
                LigneCommandeDTO ligneCommande = new LigneCommandeDTO();
                ligneCommande.setProductList(products);
                ligneCommande.setQuantity(quantites.get(i));
                ligneCommandes.add(ligneCommande);
            }
        }
        commande.setMontantTotal(prix);
        commandeServices.SaveCommand(commande,ligneCommandes);
        return "redirect:/all";
    }

    @GetMapping("/create")
    public String newCommande(Model model){
        List<PointVenteDTO> pointVenteDTOList = pointDeVenteServices.getAllPointVente();
        List<ProductDTO> productDTOList = productsServices.getAllProducts();
        List<LigneCommandeDTO> ligneCommandeDTOS = new ArrayList<>();

        model.addAttribute("produits",productDTOList);
        model.addAttribute("pointdevente",pointVenteDTOList);
        model.addAttribute("ligneCommande",ligneCommandeDTOS);
        return "formulaire_commande";
    }

    @GetMapping("/")
    public String getAllCommande(Model model){
        List<CommandeDTO> commandeDTOList = commandeServices.getAllCommande();
        model.addAttribute("commande",commandeDTOList);
        return "commande_view";
    }

    @GetMapping("/one")
    public String getOneCommande(@ModelAttribute("commande") CommandeDTO commandeId, Model model){
        CommandeDTO commandeDTO = commandeServices.getCommandeById(commandeId);
        model.addAttribute("commande",commandeDTO);
        return "one_commande";
    }


}
