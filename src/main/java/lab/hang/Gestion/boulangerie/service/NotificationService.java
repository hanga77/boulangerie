package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.Fournisseur;
import lab.hang.Gestion.boulangerie.model.Notification;

import lab.hang.Gestion.boulangerie.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void envoyerAlerteDetteElevee(Fournisseur fournisseur, double montant) {
        String message = String.format("ALERTE: La dette envers le fournisseur %s a atteint %.2f XAF",
                fournisseur.getNom(), montant);
        Notification notification = new Notification();
        notification.setType("DETTE");
        notification.setMessage(message);
        notification.setDateCreation(LocalDateTime.now());
        notification.setVue(false);
        notificationRepository.save(notification);
    }

    public void envoyerNotificationUrgente(String message) {
        Notification notification = new Notification();
        notification.setType("URGENT");
        notification.setMessage(message);
        notification.setDateCreation(LocalDateTime.now());
        notification.setVue(false);
        notification.setPriorite("HAUTE");
        notificationRepository.save(notification);
    }

    public void envoyerNotificationStockBas(String produit, int quantite) {
        String message = String.format("Stock bas pour %s: %d unités restantes", produit, quantite);
        Notification notification = new Notification();
        notification.setType("STOCK");
        notification.setMessage(message);
        notification.setDateCreation(LocalDateTime.now());
        notification.setVue(false);
        notificationRepository.save(notification);
    }
}