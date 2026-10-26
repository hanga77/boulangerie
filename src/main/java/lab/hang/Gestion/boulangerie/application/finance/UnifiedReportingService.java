package lab.hang.Gestion.boulangerie.application.finance;

import lab.hang.Gestion.boulangerie.model.Transaction;
import lab.hang.Gestion.boulangerie.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 6 P1 — CA et mouvements consolidés V1 + V2.
 */
@Service
public class UnifiedReportingService {

    private final TransactionRepository transactionRepo;

    public UnifiedReportingService(TransactionRepository transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    public record CaBreakdown(
            LocalDate debut,
            LocalDate fin,
            double venteV1,
            double venteLibreAncien,
            double livraisonV2,
            double caisseV2,
            double paiementsFacture,
            double totalCa,
            double depenses,
            double resultat,
            List<Transaction> mouvementsRevenus
    ) {}

    public CaBreakdown report(LocalDate debut, LocalDate fin) {
        if (debut == null) debut = LocalDate.now().withDayOfMonth(1);
        if (fin == null) fin = LocalDate.now();
        if (fin.isBefore(debut)) {
            LocalDate tmp = debut;
            debut = fin;
            fin = tmp;
        }

        double venteV1 = sum(TransactionTypes.VENTE, debut, fin);
        double venteLibreAncien = sum(TransactionTypes.VENTE_LIBRE, debut, fin);
        double livraisonV2 = sum(TransactionTypes.VENTE_LIVRAISON_V2, debut, fin);
        double caisseV2 = sum(TransactionTypes.VENTE_CAISSE_V2, debut, fin);
        double paiementsFacture = sum(TransactionTypes.PAIEMENT_FACTURE, debut, fin);

        double totalCa = venteV1 + venteLibreAncien + livraisonV2 + caisseV2 + paiementsFacture;
        double depenses = nz(transactionRepo.sumMontantByTypeInAndDateBetween(
                TransactionTypes.EXPENSE_TYPES, debut, fin));

        List<Transaction> mouvements = transactionRepo.findByDateBetweenAndTypeIn(
                debut, fin, TransactionTypes.REVENUE_TYPES);

        return new CaBreakdown(
                debut, fin,
                venteV1, venteLibreAncien, livraisonV2, caisseV2, paiementsFacture,
                totalCa, depenses, totalCa - depenses,
                mouvements
        );
    }

    /** Répartition pour graphiques / tableaux. */
    public Map<String, Double> caParSource(CaBreakdown b) {
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("Ventes / livraisons V1", b.venteV1());
        m.put("Ventes libres (ancien)", b.venteLibreAncien());
        m.put("Livraisons V2", b.livraisonV2());
        m.put("Caisse V2", b.caisseV2());
        m.put("Paiements facture", b.paiementsFacture());
        return m;
    }

    private double sum(String type, LocalDate debut, LocalDate fin) {
        return nz(transactionRepo.sumMontantByTypeAndDateBetween(type, debut, fin));
    }

    private static double nz(Double v) {
        return v != null ? v : 0.0;
    }
}
