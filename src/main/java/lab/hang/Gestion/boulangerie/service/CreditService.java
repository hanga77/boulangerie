package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.application.finance.TransactionTypes;
import lab.hang.Gestion.boulangerie.exception.EntityNotFoundException;
import lab.hang.Gestion.boulangerie.model.CreditReport;
import lab.hang.Gestion.boulangerie.model.Fournisseur;
import lab.hang.Gestion.boulangerie.model.FournisseurDette;
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
    private final FinanceService financeService;
    private static final double SEUIL_ALERTE = 10000.0;

    public CreditService(FournisseurDetteRepository detteRepository,
                         NotificationService notificationService,
                         FinanceService financeService) {
        this.detteRepository = detteRepository;
        this.notificationService = notificationService;
        this.financeService = financeService;
    }

    @Transactional
    public void enregistrerDette(Fournisseur fournisseur, double montant) {
        FournisseurDette dette = new FournisseurDette();
        dette.setFournisseur(fournisseur);
        dette.setMontantDette(montant);
        dette.setDateCreation(LocalDate.now());
        dette.setDateEcheance(LocalDate.now().plusDays(30));
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

    /**
 * Remboursement dette fournisseur + débit trésorerie (type DETTE_REMBOURSEMENT).
     */
    @Transactional
    public void effectuerRemboursement(Long detteId, double montant) {
        FournisseurDette dette = detteRepository.findById(detteId)
                .orElseThrow(() -> new EntityNotFoundException("Dette non trouvée"));

        if (montant <= 0) {
            throw new IllegalArgumentException("Montant de remboursement invalide");
        }

        String fournisseurNom = dette.getFournisseur() != null
                ? dette.getFournisseur().getNom() : ("#" + detteId);

        if (montant >= dette.getMontantDette()) {
            dette.setStatus("REMBOURSEE");
            dette.setMontantDette(0);
        } else {
            dette.setMontantDette(dette.getMontantDette() - montant);
        }
        detteRepository.save(dette);

        // Impact trésorerie unifiée
        try {
            Long compteId = financeService.getCompteBancairePrincipal().getId();
            financeService.creerTransaction(
                    TransactionTypes.DETTE_REMBOURSEMENT,
                    montant,
                    "Remboursement dette fournisseur " + fournisseurNom + " (dette #" + detteId + ")",
                    compteId
            );
        } catch (Exception e) {
            log.error("Échec enregistrement trésorerie remboursement dette #{}: {}", detteId, e.getMessage());
            throw new IllegalStateException(
                    "Remboursement dette enregistré mais échec trésorerie: " + e.getMessage(), e);
        }
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
