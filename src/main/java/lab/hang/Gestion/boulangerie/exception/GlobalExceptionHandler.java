package lab.hang.Gestion.boulangerie.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException ex, RedirectAttributes redirectAttributes) {
        log.warn("Utilisateur non trouvé: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(UserNotAuthenticatedException.class)
    public String handleUserNotAuthenticatedException(UserNotAuthenticatedException ex, RedirectAttributes redirectAttributes) {
        log.warn("Accès non authentifié: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Vous devez être connecté pour accéder à cette ressource");
        return "redirect:/login";
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public String handleUnauthorizedOperationException(UnauthorizedOperationException ex, RedirectAttributes redirectAttributes) {
        log.warn("Opération non autorisée: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Vous n'avez pas les droits nécessaires pour effectuer cette opération");
        return "redirect:/dashboard";
    }

    @ExceptionHandler({CommandeNotFoundException.class, ProduitNotFoundException.class,
            MatierePremiereNotFoundException.class, ProductionNotFoundException.class,
            EntityNotFoundException.class, ResourceNotFoundException.class})
    public String handleNotFoundException(RuntimeException ex, RedirectAttributes redirectAttributes) {
        log.warn("Ressource non trouvée: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(StockInsuffisantException.class)
    public String handleStockInsuffisantException(StockInsuffisantException ex, RedirectAttributes redirectAttributes) {
        log.warn("Stock insuffisant: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/matieres-premieres";
    }

    @ExceptionHandler(SoldeInsuffisantException.class)
    public String handleSoldeInsuffisantException(SoldeInsuffisantException ex, RedirectAttributes redirectAttributes) {
        log.warn("Solde insuffisant: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/finance";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationException(MethodArgumentNotValidException ex, RedirectAttributes redirectAttributes) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Erreur de validation: {}", errors);
        redirectAttributes.addFlashAttribute("errorMessage", "Données invalides : " + errors);
        return "redirect:/dashboard";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, RedirectAttributes redirectAttributes) {
        log.warn("Argument invalide: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(Exception.class)
    public String handleGlobalException(Exception ex, Model model) {
        log.error("Erreur inattendue", ex);
        model.addAttribute("errorMessage", "Une erreur inattendue s'est produite. Veuillez réessayer plus tard.");
        return "500";
    }
}
