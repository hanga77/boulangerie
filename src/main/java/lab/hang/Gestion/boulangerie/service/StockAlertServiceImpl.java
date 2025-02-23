package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.Lot;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.repository.MatierePremiereRepository;
import lab.hang.Gestion.boulangerie.repository.LotRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StockAlertServiceImpl implements StockAlertService {

    private final MatierePremiereRepository matierePremiereRepository;
    private final LotRepository lotRepository;



    public StockAlertServiceImpl(MatierePremiereRepository matierePremiereRepository, LotRepository lotRepository) {
        this.matierePremiereRepository = matierePremiereRepository;
        this.lotRepository = lotRepository;
    }

    @Override
    public void checkStockLevels() {
        List<MatierePremiere> matieresPremieres = matierePremiereRepository.findAll();
        for (MatierePremiere matiere : matieresPremieres) {
            if (matiere.isStockCritique()) {
                notifyLowStock(matiere);
            }
        }
    }


    @Override
    public void notifyLowStock(MatierePremiere matierePremiere) {
        String message = String.format("Stock critique pour %s : %f %s restant(s)",
                matierePremiere.getNom(),
                matierePremiere.getStock(),
                matierePremiere.getUniteMesure());
        //emailService.sendAlertEmail(message);
    }

    @Scheduled(cron = "0 0 * * * *") // Vérification horaire
    public void scheduleStockCheck() {
        checkStockLevels();
    }

    @Override
    public void notifyStockExpiration(Lot lot) {
        // Implémentez la logique de notification (email, SMS, etc.)
        System.out.println("Alerte: Lot " + lot.getNumeroLot() + " expire bientôt");
    }

    @Override
    public Object getCurrentAlerts() {
        return null;
    }

    public void checkExpiringLots() {
        List<Lot> lots = lotRepository.findByDatePeremptionBefore(LocalDate.now().plusDays(7));
        for (Lot lot : lots) {
            notifyStockExpiration(lot);
        }
    }
}