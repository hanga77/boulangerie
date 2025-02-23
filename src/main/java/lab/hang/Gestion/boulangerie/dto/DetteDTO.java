package lab.hang.Gestion.boulangerie.dto;


public class DetteDTO {
    private Long id;
    private String fournisseurNom;
    private double montant;
    private String dateCreation;
    private String dateEcheance;
    private String status;

    // Constructeurs, getters et setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFournisseurNom() {
        return fournisseurNom;
    }

    public void setFournisseurNom(String fournisseurNom) {
        this.fournisseurNom = fournisseurNom;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(String dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
