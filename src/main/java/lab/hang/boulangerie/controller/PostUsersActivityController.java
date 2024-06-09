package lab.hang.boulangerie.controller;


import lab.hang.boulangerie.services.UsersServices;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@AllArgsConstructor
@Controller
public class PostUsersActivityController {
    private UsersServices usersServices;


    @GetMapping("/dashboard")
    public String searchUsers(Model model){

        Object currentUserProfile = usersServices.getCurrentUsersProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            model.addAttribute("username", currentUsername);
        }
        model.addAttribute("user", currentUserProfile);
        return "dashboard";
    }
}
