package lab.hang.Gestion.boulangerie.application.inventory;

import lab.hang.Gestion.boulangerie.domain.inventory.LotV2;
import lab.hang.Gestion.boulangerie.domain.inventory.events.StockReceivedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Use case : réception fournisseur.
 * Ne touche PAS à la finance — publie StockReceivedEvent.
 */
@Service
public class ReceiveStockUseCase {

    private final InventoryPort inventoryPort;
    private final ApplicationEventPublisher events;

    public ReceiveStockUseCase(InventoryPort inventoryPort, ApplicationEventPublisher events) {
        this.inventoryPort = inventoryPort;
        this.events = events;
    }

    @Transactional
    public void execute(ReceiveStockCommand cmd) {
        if (cmd.matierePremiereId() == null) {
            throw new IllegalArgumentException("Matière première obligatoire");
        }
        double nette = cmd.quantiteNette();
        if (nette <= 0) {
            throw new IllegalArgumentException("Quantité nette doit être positive");
        }
        if (cmd.prixUnitaire() <= 0) {
            throw new IllegalArgumentException("Prix unitaire doit être positif");
        }

        int dureeJours = cmd.dureeConservationJours() != null
                ? cmd.dureeConservationJours()
                : inventoryPort.dureeConservationDefaut(cmd.matierePremiereId());

        LocalDate dlc = LocalDate.now().plusDays(dureeJours);

        LotV2 lot = new LotV2(
                cmd.matierePremiereId(),
                nette,
                cmd.prixUnitaire(),
                dlc,
                cmd.fournisseurId()
        );
        inventoryPort.saveLot(lot);
        inventoryPort.augmenterStock(cmd.matierePremiereId(), nette, cmd.prixUnitaire());
        inventoryPort.enregistrerMouvementEntree(
                cmd.matierePremiereId(),
                nette,
                cmd.quantiteCommandee(),
                cmd.quantiteAvariee(),
                cmd.userId()
        );

        events.publishEvent(StockReceivedEvent.of(
                cmd.matierePremiereId(),
                inventoryPort.nomMatiere(cmd.matierePremiereId()),
                nette,
                cmd.prixUnitaire(),
                cmd.userId(),
                cmd.username()
        ));
    }
}
