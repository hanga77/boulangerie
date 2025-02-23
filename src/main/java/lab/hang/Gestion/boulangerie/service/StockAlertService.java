package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.Lot;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;

public interface StockAlertService {
    void checkStockLevels();
    void notifyLowStock(MatierePremiere matierePremiere);
    void notifyStockExpiration(Lot lot);

    Object getCurrentAlerts();
}
