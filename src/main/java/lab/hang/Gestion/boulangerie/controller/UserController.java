package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.RegisterRequest;
import lab.hang.Gestion.boulangerie.service.AppSettingsService;
import lab.hang.Gestion.boulangerie.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;

@Controller
public class UserController {

    private final UserService userService;
    private final AppSettingsService appSettingsService;

    public UserController(UserService userService, AppSettingsService appSettingsService) {
        this.userService = userService;
        this.appSettingsService = appSettingsService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (userService.hasUsers() && !isAdmin()) {
            return "redirect:/login";
        }
        model.addAttribute("user", new RegisterRequest());
        return "register";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") RegisterRequest request) {
        if (userService.hasUsers() && !isAdmin()) {
            return "redirect:/login";
        }
        userService.registerUser(request);
        return "redirect:/login";
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/admin/users/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public String listInactiveUsers(Model model) {
        model.addAttribute("inactiveUsers", userService.getAllInactiveUsers());
        return "admin/inactive-users";
    }

    @PostMapping("/admin/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public String activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUserRole(@PathVariable Long id, @RequestParam String role) {
        userService.updateUserRole(id, role);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public String changePassword(@PathVariable Long id,
                                 @RequestParam String newPassword,
                                 RedirectAttributes ra) {
        if (newPassword == null || newPassword.length() < 8) {
            ra.addFlashAttribute("errorMessage", "Le mot de passe doit contenir au moins 8 caractères.");
            return "redirect:/admin/users";
        }
        userService.changePassword(id, newPassword);
        ra.addFlashAttribute("successMessage", "Mot de passe mis à jour.");
        return "redirect:/admin/users";
    }

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.deleteUser(id);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public String showSettings(Model model) {
        model.addAttribute("seuilIncident", appSettingsService.getSeuilIncident());
        model.addAttribute("largeurTicketMm", appSettingsService.getLargeurTicketMm());
        return "admin/settings";
    }

    @PostMapping("/admin/settings/ticket-largeur")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public String updateLargeurTicket(@RequestParam int largeur, RedirectAttributes ra) {
        if (largeur != 58 && largeur != 80) largeur = 80;
        appSettingsService.updateLargeurTicketMm(largeur);
        ra.addFlashAttribute("successMessage", "Largeur ticket mise à jour : " + largeur + " mm");
        return "redirect:/admin/settings";
    }

    @PostMapping("/admin/settings/seuil-incident")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateSeuilIncident(@RequestParam double seuil, RedirectAttributes ra) {
        if (seuil < 1 || seuil > 100) {
            ra.addFlashAttribute("errorMessage", "Le seuil doit être entre 1 et 100 %.");
            return "redirect:/admin/settings";
        }
        appSettingsService.updateSeuilIncident(seuil);
        ra.addFlashAttribute("successMessage", "Seuil d'alerte mis à jour : " + seuil + " %");
        return "redirect:/admin/settings";
    }

    @PostMapping("/admin/settings/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public String uploadLogo(@RequestParam("logo") MultipartFile file,
                             RedirectAttributes ra) throws IOException {
        if (file.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Veuillez sélectionner un fichier image.");
            return "redirect:/admin/settings";
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            ra.addFlashAttribute("errorMessage", "Le fichier doit être une image (PNG, JPG…).");
            return "redirect:/admin/settings";
        }
        File dir = new File(uploadDir).getAbsoluteFile();
        dir.mkdirs();
        file.transferTo(new File(dir, "logo.png"));
        ra.addFlashAttribute("successMessage", "Logo mis à jour avec succès.");
        return "redirect:/admin/settings";
    }

    @PostMapping("/admin/settings/logo/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public String resetLogo(RedirectAttributes ra) {
        File logo = new File(uploadDir, "logo.png").getAbsoluteFile();
        if (logo.exists()) logo.delete();
        ra.addFlashAttribute("successMessage", "Logo réinitialisé.");
        return "redirect:/admin/settings";
    }
}
