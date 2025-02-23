package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.Fournisseur;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    public void envoyerAlerteDetteElevee(Fournisseur fournisseur, double montant) {
        // Envoyer email à l'administrateur
        String message = String.format("ALERTE: La dette envers le fournisseur %s a atteint %.2f",
                fournisseur.getNom(), montant);

        // TODO: Implémenter l'envoi d'email
        System.out.println(message);
    }
}
