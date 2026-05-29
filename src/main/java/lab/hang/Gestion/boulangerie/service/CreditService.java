package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.EntityNotFoundException;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.FournisseurDetteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CreditService {
    private static final Logger log = LoggerFactory.getLogger(CreditService.class);
    private final FournisseurDetteRepository detteRepository;
    private final NotificationService notificationService;
    private static final double SEUIL_ALERTE = 10000.0;

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

        double detteTotale = calculerDetteTotale(fournisseur);
        if (detteTotale > SEUIL_ALERTE) {
            try {
                notificationService.envoyerAlerteDetteElevee(fournisseur, detteTotale);
            } catch (Exception e) {
                log.warn("Notification email non envoyée : {}", e.getMessage());
            }
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

    public List<FournisseurDette> getAllDettesEnCours() {
        return detteRepository.findByStatus("EN_COURS");
    }

    public double getTotalGlobal() {
        return detteRepository.findByStatus("EN_COURS").stream()
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
