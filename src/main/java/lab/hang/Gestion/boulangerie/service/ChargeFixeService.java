package lab.hang.Gestion.boulangerie.service;

import jakarta.transaction.Transactional;
import lab.hang.Gestion.boulangerie.dto.ChargeFixeDTO;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.ChargeFixe;
import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.repository.ChargeFixeRepository;
import lab.hang.Gestion.boulangerie.repository.CompteBancaireRepository;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChargeFixeService {

    private final ChargeFixeRepository chargeFixeRepository;
    private final TransactionRepository transactionRepository;
    private final CompteBancaireRepository compteBancaireRepository;
    private final AlerteService alerteService;

    public ChargeFixeService(ChargeFixeRepository chargeFixeRepository,
                             TransactionRepository transactionRepository,
                             CompteBancaireRepository compteBancaireRepository,
                             AlerteService alerteService) {
        this.chargeFixeRepository = chargeFixeRepository;
        this.transactionRepository = transactionRepository;
        this.compteBancaireRepository = compteBancaireRepository;
        this.alerteService = alerteService;
    }

    public ChargeFixe getById(Long id) {
        return chargeFixeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Charge fixe non trouvée : " + id));
    }

    @Transactional
    public ChargeFixe getByIdForPdf(Long id) {
        ChargeFixe c = chargeFixeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Charge fixe non trouvée : " + id));
        // Initialise le proxy lazy Transaction.compteBancaire dans la transaction
        if (c.getTransaction() != null) {
            c.getTransaction().getCompteBancaire().getNom();
        }
        return c;
    }

    public List<CompteBancaire> getAllComptesBancaires() {
        return compteBancaireRepository.findAll();
    }

    @Transactional
    public void payerChargeFixe(Long id, Long compteBancaireId) {
        ChargeFixe charge = chargeFixeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Charge fixe non trouvée : " + id));

        if (charge.isPaye()) {
            throw new IllegalStateException("Cette charge est déjà payée.");
        }

        CompteBancaire compte = compteBancaireRepository.findById(compteBancaireId)
            .orElseThrow(() -> new ResourceNotFoundException("Compte bancaire introuvable : " + compteBancaireId));

        if (compte.getSolde() < charge.getMontant()) {
            throw new IllegalStateException(
                "Solde insuffisant sur le compte " + compte.getNom()
                + " pour couvrir ce paiement.");
        }

        Transaction transaction = new Transaction();
        transaction.setType("CHARGE");
        transaction.setMontant(charge.getMontant());
        transaction.setDate(LocalDate.now());
        transaction.setDescription("Paiement " + charge.getType() + " — " + charge.getDescription());
        transaction.setCompteBancaire(compte);

        compte.setSolde(compte.getSolde() - charge.getMontant());
        compteBancaireRepository.save(compte);
        Transaction savedTx = transactionRepository.save(transaction);

        charge.setPaye(true);
        charge.setDatePaiement(LocalDate.now());
        charge.setTransaction(savedTx);
        charge.setCompteBancaire(compte);

        if (charge.getPeriodicite() != null) {
            creerProchaineEcheance(charge);
        }

        chargeFixeRepository.save(charge);
    }

    public List<ChargeFixeDTO> getChargesFixesAVenir(int joursAvant) {
        LocalDate dateDebut = LocalDate.now();
        LocalDate dateFin = dateDebut.plusDays(joursAvant);
        return chargeFixeRepository.findByDateEcheanceBetween(dateDebut, dateFin)
            .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void creerChargeFixe(ChargeFixeDTO chargeFixeDTO) {
        chargeFixeRepository.save(mapToEntity(chargeFixeDTO));
    }

    public List<ChargeFixeDTO> getAllChargesFixe() {
        return chargeFixeRepository.findAll().stream()
            .map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ChargeFixeDTO> getBilanChargesFixes() {
        return chargeFixeRepository.findAll().stream()
            .filter(ChargeFixe::isPaye).map(this::mapToDTO).collect(Collectors.toList());
    }

    private void creerProchaineEcheance(ChargeFixe charge) {
        ChargeFixe nouvelleCharge = new ChargeFixe();
        nouvelleCharge.setType(charge.getType());
        nouvelleCharge.setMontant(charge.getMontant());
        nouvelleCharge.setDescription(charge.getDescription());
        nouvelleCharge.setPeriodicite(charge.getPeriodicite());

        LocalDate prochaineEcheance = switch (charge.getPeriodicite()) {
            case "MENSUEL"      -> charge.getDateEcheance().plusMonths(1);
            case "TRIMESTRIEL"  -> charge.getDateEcheance().plusMonths(3);
            case "SEMESTRIEL"   -> charge.getDateEcheance().plusMonths(6);
            case "ANNUEL"       -> charge.getDateEcheance().plusYears(1);
            default             -> null;
        };

        if (prochaineEcheance != null) {
            nouvelleCharge.setDateEcheance(prochaineEcheance);
            nouvelleCharge.setPaye(false);
            chargeFixeRepository.save(nouvelleCharge);
            alerteService.creerAlerte(
                "CHARGE_FIXE", "INFO",
                "Nouvelle échéance créée pour " + charge.getType() + " le " + prochaineEcheance
            );
        }
    }

    private ChargeFixeDTO mapToDTO(ChargeFixe c) {
        ChargeFixeDTO dto = new ChargeFixeDTO();
        dto.setId(c.getId());
        dto.setType(c.getType());
        dto.setMontant(c.getMontant());
        dto.setDescription(c.getDescription());
        dto.setPeriodicite(c.getPeriodicite());
        dto.setDateEcheance(c.getDateEcheance());
        dto.setPaye(c.isPaye());
        return dto;
    }

    private ChargeFixe mapToEntity(ChargeFixeDTO dto) {
        ChargeFixe c = new ChargeFixe();
        c.setType(dto.getType());
        c.setMontant(dto.getMontant());
        c.setDescription(dto.getDescription());
        c.setPeriodicite(dto.getPeriodicite());
        c.setDateEcheance(dto.getDateEcheance());
        c.setPaye(dto.isPaye());
        return c;
    }
}
