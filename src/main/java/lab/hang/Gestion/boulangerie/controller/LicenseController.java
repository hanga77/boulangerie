package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.service.LicenseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/licence")
@PreAuthorize("hasRole('ADMIN')")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping
    public String page(@RequestParam(required = false) Boolean expired, Model model) {
        model.addAttribute("status", licenseService.getStatus());
        model.addAttribute("joursRestants", licenseService.demoJoursRestants());
        model.addAttribute("installedAt", licenseService.getInstalledAt());
        model.addAttribute("expired", Boolean.TRUE.equals(expired));
        return "admin/licence";
    }

    @PostMapping("/activer")
    public String activer(@RequestParam String key, RedirectAttributes ra) {
        if (licenseService.activate(key.trim())) {
            ra.addFlashAttribute("activationOk", true);
        } else {
            ra.addFlashAttribute("activationErreur", true);
        }
        return "redirect:/admin/licence";
    }
}
