package lab.hang.Gestion.boulangerie.service;

import jakarta.transaction.Transactional;
import lab.hang.Gestion.boulangerie.dto.FactureDTO;
import lab.hang.Gestion.boulangerie.exception.EntityNotFoundException;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.Facture;
import lab.hang.Gestion.boulangerie.repository.FactureRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacturationService {
    private final FactureRepository factureRepository;
    private final AlerteService alerteService;
    private final KPIService kpiService;
    //private final EmailService emailService;

    public FacturationService(FactureRepository factureRepository, AlerteService alerteService, KPIService kpiService) {
        this.factureRepository = factureRepository;
        this.alerteService = alerteService;
        this.kpiService = kpiService;
    }


    // Méthodes existantes...

    public void relancerFacturesImpayees() {
        LocalDate dateLimit = LocalDate.now().minusDays(15);
        List<Facture> facturesEnRetard = factureRepository
                .findByDateEcheanceBeforeAndStatut(dateLimit, "EMISE");

        for (Facture facture : facturesEnRetard) {
            // Envoyer email de relance
            //emailService.envoyerRelanceFacture(facture);

            // Créer une alerte
            alerteService.creerAlerte(
                    "PAIEMENT",
                    "WARNING",
                    "Facture " + facture.getNumero() + " en retard de paiement"
            );

            facture.setStatut("EN_RETARD");
            factureRepository.save(facture);
        }
    }

    public List<FactureDTO> getFacturesImpayees() {
        return factureRepository.findByStatutNot("PAYEE")
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public void enregistrerPaiement(Long factureId, String modePaiement) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée"));

        facture.setStatut("PAYEE");
        factureRepository.save(facture);

        // Enregistrer la transaction
       /* comptabiliteService.enregistrerTransaction(
                "PAIEMENT_FACTURE",
                facture.getMontantTTC(),
                "Paiement facture " + facture.getNumero() + " par " + modePaiement
        );*/

        // Mettre à jour les KPIs
        //kpiService.updateKPIsFacturation(facture);
    }

    public Object getStatistiquesFacturation() {
        return null;
    }

   /* public List<FactureDTO> getFacturesImpayees() {
        return factureRepository.findByStatut("PAYEE")
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }*/

    private FactureDTO mapToDTO(Facture facture) {
        FactureDTO factureDTO = new FactureDTO();
        factureDTO.setId(facture.getId());
        factureDTO.setNumero(facture.getNumero());
        factureDTO.setDateEmission(facture.getDateEmission());
        factureDTO.setMontantTTC(facture.getMontantTTC());
        factureDTO.setStatut(facture.getStatut());
        return factureDTO;
    }

    public Object getAllFactures() {
        return null;
    }

    public void creerFacture(FactureDTO factureDTO) {
    }

   /* public StatistiquesFacturationDTO getStatistiquesFacturation() {
        StatistiquesFacturationDTO stats = new StatistiquesFacturationDTO();

        LocalDate debut = LocalDate.now().minusMonths(1);
        LocalDate fin = LocalDate.now();

        stats.setNombreFacturesEmises(factureRepository.countByDateEmissionBetween(debut, fin));
        stats.setMontantTotalEncaisse(calculerMontantEncaisse(debut, fin));
        stats.setMontantTotalEnAttente(calculerMontantEnAttente());
        stats.setDelaiMoyenPaiement(calculerDelaiMoyenPaiement());

        return stats;
    }*/
}

