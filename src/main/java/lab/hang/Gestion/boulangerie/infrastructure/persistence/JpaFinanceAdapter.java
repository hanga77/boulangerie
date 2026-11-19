package lab.hang.Gestion.boulangerie.infrastructure.persistence;

import lab.hang.Gestion.boulangerie.application.finance.FinancePort;
import lab.hang.Gestion.boulangerie.model.CompteBancaire;
import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.repository.CompteBancaireRepository;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class JpaFinanceAdapter implements FinancePort {

    private final CompteBancaireRepository compteRepo;
    private final TransactionRepository transactionRepo;

    public JpaFinanceAdapter(CompteBancaireRepository compteRepo,
                             TransactionRepository transactionRepo) {
        this.compteRepo = compteRepo;
        this.transactionRepo = transactionRepo;
    }

    @Override
    public CompteView comptePrincipal() {
        CompteBancaire c = compteRepo.findByNom("Compte Principal")
                .orElseThrow(() -> new IllegalStateException("Compte Principal introuvable"));
        return new CompteView(c.getId(), c.getNom(), c.getSolde());
    }

    @Override
    @Transactional
    public void enregistrerTransaction(String type, double montant, String description, Long compteId) {
        CompteBancaire compte = compteRepo.findById(compteId)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable: " + compteId));
        Transaction t = new Transaction();
        t.setDate(LocalDate.now());
        t.setType(type);
        t.setMontant(montant);
        t.setDescription(description);
        t.setCompteBancaire(compte);
        transactionRepo.save(t);
    }

    @Override
    @Transactional
    public void debiterCompte(Long compteId, double montant) {
        CompteBancaire compte = compteRepo.findById(compteId)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable: " + compteId));
        compte.setSolde(compte.getSolde() - montant);
        compteRepo.save(compte);
    }
}
