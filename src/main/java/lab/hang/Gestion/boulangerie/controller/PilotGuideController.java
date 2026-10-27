package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.service.PilotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/admin/pilot")
@PreAuthorize("hasRole('ADMIN')")
public class PilotGuideController {

    private final PilotService pilotService;

    public PilotGuideController(PilotService pilotService) {
        this.pilotService = pilotService;
    }

    @GetMapping
    public String guide(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        LocalDate jour = date != null ? date : LocalDate.now();
        Map<String, Boolean> progression = pilotService.progression(jour);
        long done = progression.values().stream().filter(Boolean::booleanValue).count();
        model.addAttribute("date", jour);
        model.addAttribute("etapes", PilotService.ETAPES);
        model.addAttribute("labels", PilotService.ETAPE_LABELS);
        model.addAttribute("progression", progression);
        model.addAttribute("done", done);
        model.addAttribute("total", progression.size());
        model.addAttribute("observations", pilotService.pourJour(jour));
        return "admin/pilot";
    }

    @PostMapping("/observation")
    public String observation(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String etape,
            @RequestParam(defaultValue = "true") boolean ok,
            @RequestParam(required = false) String commentaire,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            String auteur = auth != null ? auth.getName() : "admin";
            pilotService.enregistrer(date, etape, ok, commentaire, auteur);
            ra.addFlashAttribute("successMessage",
                    (ok ? "Étape validée" : "Problème noté") + " : " +
                            PilotService.ETAPE_LABELS.getOrDefault(etape.toUpperCase(), etape));
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        String redirect = "redirect:/admin/pilot";
        if (date != null) {
            redirect += "?date=" + date;
        }
        return redirect;
    }

    @GetMapping("/observations")
    public String liste(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        if (date != null) {
            model.addAttribute("observations", pilotService.pourJour(date));
            model.addAttribute("date", date);
        } else {
            model.addAttribute("observations", pilotService.recentes());
            model.addAttribute("date", null);
        }
        model.addAttribute("labels", PilotService.ETAPE_LABELS);
        return "admin/pilot-observations";
    }
}
