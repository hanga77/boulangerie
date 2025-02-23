package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.EntityNotFoundException;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.FournisseurDetteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class CreditService {
    private final FournisseurDetteRepository detteRepository;
    private final NotificationService notificationService;
    private static final double SEUIL_ALERTE = 10000.0; // Seuil d'alerte en devise locale

    public CreditService(FournisseurDetteRepository detteRepository,
                         NotificationService notificationService) {
        this.detteRepository = detteRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void enregistrerDette(Fournisseur fournisseur, double montant) {
        FournisseurDette dette = new FournisseurDette();
        dette.setFournisseur(fournisseur);
        dette.setMontantDette(montant);
        dette.setDateCreation(LocalDate.now());
        dette.setDateEcheance(LocalDate.now().plusDays(30)); // 30 jours par défaut
        dette.setStatus("EN_COURS");

        detteRepository.save(dette);

        // Vérifier le seuil d'alerte
        double detteTotale = calculerDetteTotale(fournisseur);
        if (detteTotale > SEUIL_ALERTE) {
            notificationService.envoyerAlerteDetteElevee(fournisseur, detteTotale);
        }
    }

    @Transactional
    public void effectuerRemboursement(Long detteId, double montant) {
        FournisseurDette dette = detteRepository.findById(detteId)
                .orElseThrow(() -> new EntityNotFoundException("Dette non trouvée"));

        if (montant >= dette.getMontantDette()) {
            dette.setStatus("REMBOURSEE");
            dette.setMontantDette(0);
        } else {
            dette.setMontantDette(dette.getMontantDette() - montant);
        }

        detteRepository.save(dette);
    }

    public double calculerDetteTotale(Fournisseur fournisseur) {
        return detteRepository.findByFournisseurId(fournisseur.getId()).stream()
                .filter(dette -> "EN_COURS".equals(dette.getStatus()))
                .mapToDouble(FournisseurDette::getMontantDette)
                .sum();
    }

    public CreditReport genererRapportCredit(Fournisseur fournisseur) {
        List<FournisseurDette> dettes = detteRepository.findByFournisseurId(fournisseur.getId());

        CreditReport rapport = new CreditReport();
        rapport.setFournisseur(fournisseur);
        rapport.setDetteTotale(calculerDetteTotale(fournisseur));
        rapport.setDettes(dettes);
        rapport.setDateGeneration(LocalDate.now());

        return rapport;
    }
}
