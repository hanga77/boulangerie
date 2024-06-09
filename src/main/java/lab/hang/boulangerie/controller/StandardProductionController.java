package lab.hang.boulangerie.controller;

import jakarta.validation.Valid;
import lab.hang.boulangerie.dto.LigneStandardDTO;
import lab.hang.boulangerie.dto.MatiereDTO;
import lab.hang.boulangerie.dto.ProductDTO;
import lab.hang.boulangerie.dto.StandardProductionDTO;
import lab.hang.boulangerie.services.MatiereServices;
import lab.hang.boulangerie.services.ProductsServices;
import lab.hang.boulangerie.services.StandardProductionServices;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/standard/production")
@AllArgsConstructor
public class StandardProductionController {
    private final StandardProductionServices standardProductionServices;
    private final ProductsServices productionServices;
    private final MatiereServices matiereServices;

    @GetMapping("/")
    public String getAllStandard(Model model){
        List<StandardProductionDTO> standardProductionDTO = standardProductionServices.getAllStandard();
        return "standard_production";
    }

    @GetMapping("/create")
    public String creationStandard(Model model){
        List<ProductDTO> productDTOList = productionServices.getAllProducts();
        List<MatiereDTO> matiereDTOList = matiereServices.getAllMatiere();
        List<Integer> quantiteMatiere = new ArrayList<>();

        model.addAttribute("products", productDTOList);
        model.addAttribute("quantite", quantiteMatiere);
        model.addAttribute("matierePremiere", matiereDTOList);

        return "creer_standard";
    }

    @DeleteMapping ("/delete")
    public String deleteStandard(@Valid @ModelAttribute("standard") StandardProductionDTO standardProductionDTO){
        standardProductionServices.DeleteStandard(standardProductionDTO);
        return "redirect:/";
    }

    @PostMapping("/save")
    public String saveAllStandard(@Valid @ModelAttribute("produits") String produits,
                                   @Valid @ModelAttribute("quantite") List<Integer> quantite,
                                   @Valid @ModelAttribute("matiere") List<String> matiere){
        StandardProductionDTO standardProductionDTO = new StandardProductionDTO();
        standardProductionDTO.setProducts(productionServices.getProducts(produits));
        List<LigneStandardDTO> standardDTOS = new ArrayList<>();
        for (String mat : matiere){
            int index = matiere.indexOf(mat);
            if (quantite.get(index)!=0){
                LigneStandardDTO ligneStandardDTO = new LigneStandardDTO();
                ligneStandardDTO.setQuantity(quantite.get(index));
                ligneStandardDTO.setMatiere(matiereServices.getMatiereByName(mat));
                standardDTOS.add(ligneStandardDTO);
            }
        }
        standardProductionServices.SaveStandardProd(standardProductionDTO,standardDTOS);
        return "redirect:/";
    }
}
