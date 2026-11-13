package lab.hang.Gestion.boulangerie.application.finance;

import lab.hang.Gestion.boulangerie.domain.inventory.events.StockReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Écoute StockReceivedEvent et crée la transaction financière.
 * Remplace le couplage direct StockService → CompteBancaire.
 */
@Component
public class StockReceivedFinanceListener {

    private static final Logger log = LoggerFactory.getLogger(StockReceivedFinanceListener.class);

    private final FinancePort financePort;

    public StockReceivedFinanceListener(FinancePort financePort) {
        this.financePort = financePort;
    }

    @EventListener
    @Transactional
    public void onStockReceived(StockReceivedEvent event) {
        var compte = financePort.comptePrincipal();
        boolean credit = compte.solde() < event.coutTotal();
        String type = credit ? "ACHAT_CREDIT" : "ACHAT";

        String description = String.format(
                "Achat de %.2f unités de %s par %s%s",
                event.quantiteNette(),
                event.matiereNom(),
                event.username(),
                credit ? " (CRÉDIT)" : ""
        );

        if (credit) {
            log.warn("Achat à crédit — user={}, montant={}, solde={}",
                    event.username(), event.coutTotal(), compte.solde());
        }

        financePort.enregistrerTransaction(
                type,
                event.coutTotal(),
                description,
                compte.id()
        );
        financePort.debiterCompte(compte.id(), event.coutTotal());
    }
}
