package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.model.Employe;
import lab.hang.Gestion.boulangerie.service.EmployeService;
import lab.hang.Gestion.boulangerie.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employes")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class EmployeController {

    private final EmployeService employeService;
    private final UserService userService;

    public EmployeController(EmployeService employeService, UserService userService) {
        this.employeService = employeService;
        this.userService    = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employes", employeService.getAllActifs());
        return "employes/list";
    }

    @GetMapping("/nouveau")
    public String showCreateForm(Model model) {
        model.addAttribute("employe", new Employe());
        model.addAttribute("users", userService.getAllUsers());
        return "employes/form";
    }

    @PostMapping("/nouveau")
    public String create(@ModelAttribute Employe employe,
                         @RequestParam(required = false) Long userId,
                         RedirectAttributes ra) {
        if (userId != null) {
            employe.setUser(userService.getUserById(userId));
        }
        employeService.save(employe);
        ra.addFlashAttribute("successMessage", "Employé créé avec succès.");
        return "redirect:/employes";
    }

    @GetMapping("/{id}/modifier")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("employe", employeService.getById(id));
        model.addAttribute("users", userService.getAllUsers());
        return "employes/form";
    }

    @PostMapping("/{id}/modifier")
    public String update(@PathVariable Long id,
                         @ModelAttribute Employe employe,
                         @RequestParam(required = false) Long userId,
                         RedirectAttributes ra) {
        Employe existing = employeService.getById(id);
        existing.setNom(employe.getNom());
        existing.setPrenom(employe.getPrenom());
        existing.setPoste(employe.getPoste());
        existing.setSalaireBase(employe.getSalaireBase());
        existing.setDateEmbauche(employe.getDateEmbauche());
        existing.setNumeroCnps(employe.getNumeroCnps());
        existing.setUser(userId != null ? userService.getUserById(userId) : null);
        employeService.save(existing);
        ra.addFlashAttribute("successMessage", "Employé mis à jour.");
        return "redirect:/employes";
    }

    @PostMapping("/{id}/desactiver")
    public String desactiver(@PathVariable Long id, RedirectAttributes ra) {
        employeService.desactiver(id);
        ra.addFlashAttribute("successMessage", "Employé désactivé.");
        return "redirect:/employes";
    }
}
