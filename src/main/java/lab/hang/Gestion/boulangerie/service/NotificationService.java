package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.Facture;
import lab.hang.Gestion.boulangerie.model.Fournisseur;
import lab.hang.Gestion.boulangerie.model.Notification;

import lab.hang.Gestion.boulangerie.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(EmailService emailService, NotificationRepository notificationRepository) {
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
    }

    public void envoyerAlerteDetteElevee(Fournisseur fournisseur, double montant) {
        String message = String.format("ALERTE: La dette envers le fournisseur %s a atteint %.2f XAF",
                fournisseur.getNom(), montant);

        // Enregistrer la notification
        Notification notification = new Notification();
        notification.setType("DETTE");
        notification.setMessage(message);
        notification.setDateCreation(LocalDateTime.now());
        notification.setVue(false);
        notificationRepository.save(notification);

        // Envoyer un email
        emailService.envoyerEmailGeneral("admin@boulangerie.com",
                "Alerte dette élevée", message, false);
    }

    public void envoyerNotificationUrgente(String message) {
        // Enregistrer la notification
        Notification notification = new Notification();
        notification.setType("URGENT");
        notification.setMessage(message);
        notification.setDateCreation(LocalDateTime.now());
        notification.setVue(false);
        notification.setPriorite("HAUTE");
        notificationRepository.save(notification);

        // Envoyer un email
        emailService.envoyerEmailGeneral("admin@boulangerie.com",
                "Notification URGENTE", message, false);

        // Envoyer un SMS via un service externe (à implémenter)
        // smsService.envoyerSMS("0123456789", message);
    }

    public void envoyerNotificationStockBas(String produit, int quantite) {
        String message = String.format("Stock bas pour %s: %d unités restantes", produit, quantite);

        Notification notification = new Notification();
        notification.setType("STOCK");
        notification.setMessage(message);
        notification.setDateCreation(LocalDateTime.now());
        notification.setVue(false);
        notificationRepository.save(notification);

        emailService.envoyerEmailGeneral("stock@boulangerie.com",
                "Alerte stock bas", message, false);
    }
}