package lab.hang.Gestion.boulangerie.service;

import jakarta.transaction.Transactional;
import lab.hang.Gestion.boulangerie.dto.FactureDTO;
import lab.hang.Gestion.boulangerie.exception.EntityNotFoundException;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.model.Facture;
import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.repository.CompteBancaireRepository;
import lab.hang.Gestion.boulangerie.repository.FactureRepository;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacturationService {
    private final FactureRepository factureRepository;
    private final AlerteService alerteService;
    private final KPIService kpiService;
    private final TransactionRepository transactionRepository;
    private final CompteBancaireRepository compteBancaireRepository;

    public FacturationService(FactureRepository factureRepository,
                              AlerteService alerteService,
                              KPIService kpiService,
                              TransactionRepository transactionRepository,
                              CompteBancaireRepository compteBancaireRepository) {
        this.factureRepository = factureRepository;
        this.alerteService = alerteService;
        this.kpiService = kpiService;
        this.transactionRepository = transactionRepository;
        this.compteBancaireRepository = compteBancaireRepository;
    }

    public List<FactureDTO> getAllFactures() {
        return factureRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void creerFacture(FactureDTO factureDTO) {
        Facture facture = new Facture();
        facture.setNumero(generateNumero());
        facture.setType(factureDTO.getType());
        facture.setMontantHT(factureDTO.getMontantHT());
        facture.setMontantTVA(factureDTO.getMontantTVA());
        facture.setMontantTTC(factureDTO.getMontantHT() + factureDTO.getMontantTVA());
        facture.setDateEmission(factureDTO.getDateEmission() != null
                ? factureDTO.getDateEmission() : LocalDate.now());
        facture.setDateEcheance(factureDTO.getDateEcheance() != null
                ? factureDTO.getDateEcheance() : LocalDate.now().plusDays(30));
        facture.setStatut("EMISE");
        factureRepository.save(facture);
    }

    public Map<String, Object> getStatistiquesFacturation() {
        List<Facture> all = factureRepository.findAll();
        double totalFacture = all.stream().mapToDouble(Facture::getMontantTTC).sum();
        double totalEncaisse = all.stream()
                .filter(f -> "PAYEE".equals(f.getStatut()))
                .mapToDouble(Facture::getMontantTTC).sum();
        double totalImpaye = all.stream()
                .filter(f -> !"PAYEE".equals(f.getStatut()))
                .mapToDouble(Facture::getMontantTTC).sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFacture", totalFacture);
        stats.put("totalEncaisse", totalEncaisse);
        stats.put("totalImpaye", totalImpaye);
        stats.put("nombreFactures", all.size());
        return stats;
    }

    @Transactional
    public void enregistrerPaiement(Long factureId, String modePaiement) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée : " + factureId));

        facture.setStatut("PAYEE");
        factureRepository.save(facture);

        CompteBancaire compte = compteBancaireRepository.findByNom("Compte Principal")
                .orElseThrow(() -> new ResourceNotFoundException("Compte bancaire principal non trouvé"));
        compte.setSolde(compte.getSolde() + facture.getMontantTTC());
        compteBancaireRepository.save(compte);

        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("PAIEMENT_FACTURE");
        transaction.setMontant(facture.getMontantTTC());
        transaction.setDescription("Paiement facture " + facture.getNumero() + " par " + modePaiement);
        transaction.setCompteBancaire(compte);
        transactionRepository.save(transaction);
    }

    public void relancerFacturesImpayees() {
        LocalDate dateLimit = LocalDate.now().minusDays(15);
        List<Facture> facturesEnRetard = factureRepository
                .findByDateEcheanceBeforeAndStatut(dateLimit, "EMISE");

        for (Facture facture : facturesEnRetard) {
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

    private String generateNumero() {
        String prefix = "FAC-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        long seq = factureRepository.count() + 1;
        return prefix + String.format("%04d", seq);
    }

    private FactureDTO mapToDTO(Facture facture) {
        FactureDTO dto = new FactureDTO();
        dto.setId(facture.getId());
        dto.setNumero(facture.getNumero());
        dto.setDateEmission(facture.getDateEmission());
        dto.setDateEcheance(facture.getDateEcheance());
        dto.setMontantHT(facture.getMontantHT());
        dto.setMontantTVA(facture.getMontantTVA());
        dto.setMontantTTC(facture.getMontantTTC());
        dto.setStatut(facture.getStatut());
        dto.setType(facture.getType());
        if (facture.getClient() != null) {
            dto.setClientId(facture.getClient().getId());
        }
        return dto;
    }
}
