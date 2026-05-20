package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.RegisterRequest;
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

    public UserController(UserService userService) {
        this.userService = userService;
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

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public String showSettings() {
        return "admin/settings";
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
