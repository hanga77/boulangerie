package lab.hang.Gestion.boulangerie.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(UserNotAuthenticatedException.class)
    public String handleUserNotAuthenticatedException(UserNotAuthenticatedException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Vous devez être connecté pour accéder à cette ressource");
        return "redirect:/login";
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public String handleUnauthorizedOperationException(UnauthorizedOperationException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Vous n'avez pas les droits nécessaires pour effectuer cette opération");
        return "redirect:/dashboard";
    }

    @ExceptionHandler(Exception.class)
    public String handleGlobalException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Une erreur inattendue s'est produite. Veuillez réessayer plus tard.");
        return "500";
    }
}