package lab.hang.boulangerie.controller;

import jakarta.validation.Valid;
import lab.hang.boulangerie.dto.MouvementMatiereDTO;
import lab.hang.boulangerie.entity.Matiere;
import lab.hang.boulangerie.services.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@Controller
@RequestMapping("/Mouv")
@AllArgsConstructor
public class MouvementMatiereController {
    private final MouvementMatiereServices mouvementMatiereServices;
    private final MatiereServices matiereServices;
    private final ProductsServices productsServices;
    private  final CommandeServices commandeServices;



    @PostMapping ("/save")
    public String saveMouvement(@RequestParam("actions") String actions,
                                @Valid  @ModelAttribute("mouvement")MouvementMatiereDTO mouvementMatiereDTO,
                                @Valid @ModelAttribute("matiere")String matiereDTO){
        mouvementMatiereDTO.setMatiereID(matiereServices.getMatiereByName(matiereDTO));
        mouvementMatiereDTO.setActions(actions);
        //user missing
        mouvementMatiereServices.SaveMouvMatiere(mouvementMatiereDTO);

        return "redirect:/";
    }

    @GetMapping("/create")
    public String createMouvement(Model model){
        MouvementMatiereDTO mouvementMatiereDTO = new MouvementMatiereDTO();
        Map<Matiere, Integer> matiereQuantity = commandeServices.getMatiereCommande();
        model.addAttribute("matiereQuantite",matiereQuantity);
        model.addAttribute("mouvement",mouvementMatiereDTO);
        return "create_commande";
    }
}
