package lab.hang.Gestion.boulangerie.application.finance;

public interface FinancePort {

    record CompteView(Long id, String nom, double solde) {}

    CompteView comptePrincipal();

    void enregistrerTransaction(String type, double montant, String description, Long compteId);

    void debiterCompte(Long compteId, double montant);
}
