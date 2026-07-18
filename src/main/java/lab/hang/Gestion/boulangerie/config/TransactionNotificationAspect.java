package lab.hang.Gestion.boulangerie.config;

import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.service.NotificationService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Notifie l'admin (dashboard) à chaque transaction, quel que soit le service qui l'a créée —
 * revenu de livraison, vente guichet, paiement de charge fixe, etc. Un seul point d'accroche
 * sur le repository plutôt qu'un hook par service, pour ne pas dépendre de la liste des
 * appelants (une dizaine à ce jour) et couvrir automatiquement les futurs cas.
 */
@Aspect
@Component
public class TransactionNotificationAspect {

    private final NotificationService notificationService;

    public TransactionNotificationAspect(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @AfterReturning(
            pointcut = "execution(* lab.hang.Gestion.boulangerie.repository.TransactionRepository.save(..))",
            returning = "result")
    public void notifierTransaction(Object result) {
        if (result instanceof Transaction transaction) {
            String compteNom = transaction.getCompteBancaire() != null
                    ? transaction.getCompteBancaire().getNom() : "?";
            notificationService.notifierTransaction(
                    transaction.getType(), transaction.getMontant(),
                    transaction.getDescription(), compteNom);
        }
    }
}
