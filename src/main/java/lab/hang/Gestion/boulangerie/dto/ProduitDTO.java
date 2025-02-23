package lab.hang.Gestion.boulangerie.dto;



import java.util.Map;


public class ProduitDTO {

    private Long id;
    private String nom;
    private double prix;
    private double quantiteAttendue;
    private Map<Long, Double> matieresPremieres; // Map<MatierePremiereId, Quantité nécessaire>

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public double getQuantiteAttendue() {
        return quantiteAttendue;
    }

    public void setQuantiteAttendue(double quantiteAttendue) {
        this.quantiteAttendue = quantiteAttendue;
    }

    public Map<Long, Double> getMatieresPremieres() {
        return matieresPremieres;
    }

    public void setMatieresPremieres(Map<Long, Double> matieresPremieres) {
        this.matieresPremieres = matieresPremieres;
    }
}
