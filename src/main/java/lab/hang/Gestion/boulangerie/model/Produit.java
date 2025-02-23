package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;

import java.util.Map;

@Entity
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private double prix;

    @Column(nullable = false)
    private double quantiteAttendue; // Renommé pour plus de clarté

    @ElementCollection
    @CollectionTable(name = "produit_matiere_premiere", joinColumns = @JoinColumn(name = "produit_id"))
    @MapKeyJoinColumn(name = "matiere_premiere_id")
    @Column(name = "quantite")
    private Map<MatierePremiere, Double> matieresPremieres; // Map<MatierePremiere, Quantité nécessaire>

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

    public Map<MatierePremiere, Double> getMatieresPremieres() {
        return matieresPremieres;
    }

    public void setMatieresPremieres(Map<MatierePremiere, Double> matieresPremieres) {
        this.matieresPremieres = matieresPremieres;
    }
}