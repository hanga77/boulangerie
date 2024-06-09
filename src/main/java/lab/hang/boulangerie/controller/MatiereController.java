package lab.hang.boulangerie.controller;

import jakarta.validation.Valid;
import lab.hang.boulangerie.dto.MatiereDTO;
import lab.hang.boulangerie.services.MatiereServices;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/matiere")
public class MatiereController {
    private  final MatiereServices matiereServices;

    @GetMapping("/")
    public String getAllMatiereIndex(Model model){
        List<MatiereDTO> matiereDTOList = matiereServices.getAllMatiere();

        model.addAttribute("matiere",matiereDTOList);
        return "index_matiere";
    }

    @PutMapping("/update")
    public String updateMatiere(@Valid @ModelAttribute("matiere") MatiereDTO matiereDTO){
        matiereServices.UpdateMatiere(matiereDTO);
        return "redirect:/";
    }

    @PutMapping("/delete")
    public String deleteMatiere(@Valid @ModelAttribute("matiere") MatiereDTO matiereDTO){
        matiereServices.DeleteMatiere(matiereDTO);
        return "redirect:/";
    }
}
