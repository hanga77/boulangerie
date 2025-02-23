package lab.hang.Gestion.boulangerie.controller;


import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.service.UserService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    @GetMapping("/login")
    public String showRegistrationForm() {

        return "login";
    }
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user) {
        userService.registerUser(user);
        return "redirect:/login";
    }

    @GetMapping("/admin/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/admin/users/inactive")
    public String listInactiveUsers(Model model) {
        model.addAttribute("inactiveUsers", userService.getAllInactiveUsers());
        return "admin/inactive-users";
    }

    @PostMapping("/admin/users/{id}/activate")
    public String activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/role")
    public String updateUserRole(@PathVariable Long id, @RequestParam String role) {
        userService.updateUserRole(id, role);
        return "redirect:/admin/users";
    }

    @DeleteMapping("/admin/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}