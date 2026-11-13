package lab.hang.Gestion.boulangerie.service;

import jakarta.transaction.Transactional;
import lab.hang.Gestion.boulangerie.application.finance.TransactionTypes;
import lab.hang.Gestion.boulangerie.dto.TransactionDTO;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.mapper.TransactionMapper;
import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.repository.CompteBancaireRepository;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinanceService {

    public static final String NOM_COMPTE_PRINCIPAL = "Compte Principal";

    private final TransactionRepository transactionRepository;
    private final CompteBancaireRepository compteBancaireRepository;
    private final AlerteService alerteService;

    public FinanceService(TransactionRepository transactionRepository,
                          CompteBancaireRepository compteBancaireRepository,
                          AlerteService alerteService) {
        this.transactionRepository = transactionRepository;
        this.compteBancaireRepository = compteBancaireRepository;
        this.alerteService = alerteService;
    }

    public double calculerProfit(LocalDate startDate, LocalDate endDate) {
        double revenus = nz(transactionRepository.sumMontantByTypeInAndDateBetween(
                TransactionTypes.REVENUE_TYPES, startDate, endDate));
        double depenses = nz(transactionRepository.sumMontantByTypeInAndDateBetween(
                TransactionTypes.EXPENSE_TYPES, startDate, endDate));
        return revenus - depenses;
    }

    public CompteBancaire getCompteBancaire(Long id) {
        return compteBancaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte bancaire non trouvé avec l'ID : " + id));
    }

    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByDateBetween(startDate, endDate);
    }

    public double calculerCoutsProduction() {
        return nz(transactionRepository.sumMontantByType(TransactionTypes.PRODUCTION));
    }

    public double calculerRevenusVentes() {
        return nz(transactionRepository.sumMontantByTypeIn(TransactionTypes.REVENUE_TYPES));
    }

    public double calculerProfitTotal() {
        return calculerRevenusVentes() - calculerCoutsProduction() - calculerDepensesAchats();
    }

    public double calculerDepensesAchats() {
        return nz(transactionRepository.sumMontantByType(TransactionTypes.ACHAT));
    }

    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Page<TransactionDTO> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(TransactionMapper::toDTO);
    }

    public double calculerRevenusTotaux() {
        return calculerRevenusVentes();
    }

    public double calculerDepensesTotales() {
        return nz(transactionRepository.sumMontantByTypeIn(TransactionTypes.EXPENSE_TYPES));
    }

    public double calculerProfit() {
        return calculerProfitTotal();
    }

    public CompteBancaire getCompteBancairePrincipal() {
        return compteBancaireRepository.findByNom(NOM_COMPTE_PRINCIPAL)
                .orElseThrow(() -> new ResourceNotFoundException("Compte Principal non trouvé"));
    }

    public void creerCompteBancaire(String nom, double soldeInitial) {
        CompteBancaire compteBancaire = new CompteBancaire();
        compteBancaire.setSolde(soldeInitial);
        compteBancaire.setNom(nom);
        compteBancaireRepository.save(compteBancaire);
    }

    /**
     * F3 — crée le Compte Principal s'il n'existe pas (idempotent).
     * @return true si création, false si déjà présent
     */
    @Transactional
    public boolean assurerComptePrincipal(double soldeInitial) {
        if (compteBancaireRepository.findByNom(NOM_COMPTE_PRINCIPAL).isPresent()) {
            return false;
        }
        creerCompteBancaire(NOM_COMPTE_PRINCIPAL, soldeInitial);
        return true;
    }

    public TransactionDTO getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(TransactionMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée avec l'ID : " + id));
    }

    @Transactional
    public TransactionDTO creerTransaction(String type, double montant, String description, Long compteId) {
        CompteBancaire compte = compteBancaireRepository.findById(compteId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte bancaire non trouvé"));

        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setMontant(montant);
        transaction.setDescription(description);
        transaction.setDate(LocalDate.now());
        transaction.setCompteBancaire(compte);

        if (TransactionTypes.isRevenue(type)) {
            compte.setSolde(compte.getSolde() + montant);
        } else if (TransactionTypes.EXPENSE_TYPES.contains(type)
                || "PRODUCTION".equals(type) || "ACHAT".equals(type)) {
            compte.setSolde(compte.getSolde() - montant);
            if (compte.getSolde() < 1000) {
                alerteService.creerAlerte("FINANCE", "WARNING",
                        "Solde bas sur le compte " + compte.getNom() + ": " + compte.getSolde() + " XAF");
            }
        }

        compteBancaireRepository.save(compte);
        return TransactionMapper.toDTO(transactionRepository.save(transaction));
    }

    public List<CompteBancaire> getAllComptesBancaires() {
        return compteBancaireRepository.findAll();
    }

    @Transactional
    public void transfertEntreCcomptes(Long sourceId, Long destinationId, double montant, String description) {
        CompteBancaire sourceCompte = compteBancaireRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte source non trouvé"));
        CompteBancaire destinationCompte = compteBancaireRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte destination non trouvé"));

        if (sourceCompte.getSolde() < montant) {
            throw new IllegalArgumentException("Solde insuffisant pour effectuer le transfert");
        }

        sourceCompte.setSolde(sourceCompte.getSolde() - montant);
        destinationCompte.setSolde(destinationCompte.getSolde() + montant);
        compteBancaireRepository.save(sourceCompte);
        compteBancaireRepository.save(destinationCompte);

        Transaction debit = new Transaction();
        debit.setType("TRANSFERT_DEBIT");
        debit.setMontant(montant);
        debit.setDescription("Transfert vers " + destinationCompte.getNom() + ": " + description);
        debit.setDate(LocalDate.now());
        debit.setCompteBancaire(sourceCompte);

        Transaction credit = new Transaction();
        credit.setType("TRANSFERT_CREDIT");
        credit.setMontant(montant);
        credit.setDescription("Transfert depuis " + sourceCompte.getNom() + ": " + description);
        credit.setDate(LocalDate.now());
        credit.setCompteBancaire(destinationCompte);

        transactionRepository.save(debit);
        transactionRepository.save(credit);
    }

    public double calculerDepensesSalariales() {
        return nz(transactionRepository.sumMontantByTypeIn(
                List.of(TransactionTypes.SALAIRE, TransactionTypes.SALAIRE_ENFOIS)));
    }

    public double calculerRevenusTotaux(LocalDate debut, LocalDate fin) {
        return nz(transactionRepository.sumMontantByTypeInAndDateBetween(
                TransactionTypes.REVENUE_TYPES, debut, fin));
    }

    public double calculerDepensesTotales(LocalDate debut, LocalDate fin) {
        return nz(transactionRepository.sumMontantByTypeInAndDateBetween(
                TransactionTypes.EXPENSE_TYPES, debut, fin));
    }

    public double calculerDepensesSalariales(LocalDate debut, LocalDate fin) {
        return nz(transactionRepository.sumMontantByTypeInAndDateBetween(
                List.of(TransactionTypes.SALAIRE, TransactionTypes.SALAIRE_ENFOIS), debut, fin));
    }

    private static double nz(Double v) {
        return v != null ? v : 0.0;
    }
}
