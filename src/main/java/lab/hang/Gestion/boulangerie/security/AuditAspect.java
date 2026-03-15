package lab.hang.Gestion.boulangerie.security;

import lab.hang.Gestion.boulangerie.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect AOP pour l'audit automatique des opérations de création, modification et suppression.
 * Intercepte tous les appels aux méthodes save*, create*, update*, delete* des services.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @AfterReturning(
            pointcut = "execution(* lab.hang.Gestion.boulangerie.service.CommandeService.createCommande(..))",
            returning = "result")
    public void auditCreateCommande(JoinPoint jp, Object result) {
        auditService.log("CREATE_COMMANDE", "Commande", extractId(result),
                "Args: " + Arrays.toString(jp.getArgs()));
    }

    @AfterReturning(
            pointcut = "execution(* lab.hang.Gestion.boulangerie.service.CommandeService.updateCommande(..))")
    public void auditUpdateCommande(JoinPoint jp) {
        Object[] args = jp.getArgs();
        Long id = args.length > 0 ? (Long) args[0] : null;
        auditService.log("UPDATE_COMMANDE", "Commande", id, null);
    }

    @AfterReturning(
            pointcut = "execution(* lab.hang.Gestion.boulangerie.service.CommandeService.deleteCommande(..))")
    public void auditDeleteCommande(JoinPoint jp) {
        Object[] args = jp.getArgs();
        Long id = args.length > 0 ? (Long) args[0] : null;
        auditService.log("DELETE_COMMANDE", "Commande", id, null);
    }

    @AfterReturning(
            pointcut = "execution(* lab.hang.Gestion.boulangerie.service.StockService.addStock(..))")
    public void auditAddStock(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String details = args.length >= 2 ? "matiereId=" + args[0] + ", quantite=" + args[1] : null;
        auditService.log("ADD_STOCK", "MatierePremiere", args.length > 0 ? (Long) args[0] : null, details);
    }

    @AfterReturning(
            pointcut = "execution(* lab.hang.Gestion.boulangerie.service.StockService.removeStock(..))")
    public void auditRemoveStock(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String details = args.length >= 2 ? "matiereId=" + args[0] + ", quantite=" + args[1] : null;
        auditService.log("REMOVE_STOCK", "MatierePremiere", args.length > 0 ? (Long) args[0] : null, details);
    }

    @AfterReturning(
            pointcut = "execution(* lab.hang.Gestion.boulangerie.service.UserService.registerUser(..))")
    public void auditRegisterUser(JoinPoint jp) {
        auditService.log("REGISTER_USER", "User", null, null);
    }

    @AfterReturning(
            pointcut = "execution(* lab.hang.Gestion.boulangerie.service.UserService.activateUser(..))",
            returning = "result")
    public void auditActivateUser(JoinPoint jp, Object result) {
        Object[] args = jp.getArgs();
        Long id = args.length > 0 ? (Long) args[0] : null;
        auditService.log("ACTIVATE_USER", "User", id, null);
    }

    @AfterReturning(
            pointcut = "execution(* lab.hang.Gestion.boulangerie.service.UserService.updateUserRole(..))")
    public void auditUpdateUserRole(JoinPoint jp) {
        Object[] args = jp.getArgs();
        Long id = args.length > 0 ? (Long) args[0] : null;
        String role = args.length > 1 ? (String) args[1] : null;
        auditService.log("UPDATE_USER_ROLE", "User", id, "nouveauRole=" + role);
    }

    @AfterReturning(
            pointcut = "execution(* lab.hang.Gestion.boulangerie.service.UserService.deleteUser(..))")
    public void auditDeleteUser(JoinPoint jp) {
        Object[] args = jp.getArgs();
        Long id = args.length > 0 ? (Long) args[0] : null;
        auditService.log("DELETE_USER", "User", id, null);
    }

    private Long extractId(Object obj) {
        if (obj == null) return null;
        try {
            return (Long) obj.getClass().getMethod("getId").invoke(obj);
        } catch (Exception e) {
            log.debug("Impossible d'extraire l'ID de {}", obj.getClass().getSimpleName());
            return null;
        }
    }
}
