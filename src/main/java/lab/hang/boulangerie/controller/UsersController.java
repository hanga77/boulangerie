package lab.hang.boulangerie.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lab.hang.boulangerie.dto.UsersDTO;
import lab.hang.boulangerie.dto.UsersTypeDTO;
import lab.hang.boulangerie.services.UsersServices;
import lab.hang.boulangerie.services.UsersTypeServices;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@AllArgsConstructor
public class UsersController {
    private UsersTypeServices usersTypeServices;
    private UsersServices usersServices;

    @GetMapping("/register")
    public  String register(Model model){
        List<UsersTypeDTO> usersTypeDTOList = usersTypeServices.getAll();
        model.addAttribute("listUsers", usersTypeDTOList);
        model.addAttribute("users", new UsersDTO());
        return "register";
    }
    @PostMapping("/register/new")
    public  String usersRegister(
            @Valid  @ModelAttribute("users") UsersDTO usersDTO,
            Model model){
        usersServices.addNew(usersDTO);
        return "dashboard";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response,authentication);
        }
        return "logout";
    }
}
