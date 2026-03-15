package lab.hang.Gestion.boulangerie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;

public class ProduitDTO {

    private Long id;

    @NotBlank(message = "Le nom du produit est obligatoire")
    private String nom;

    @Positive(message = "Le prix doit être positif")
    private double prix;

    @Positive(message = "La quantité attendue doit être positive")
    private double quantiteAttendue;

    @NotNull(message = "Les matières premières sont obligatoires")
    private Map<Long, Double> matieresPremieres;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public double getQuantiteAttendue() { return quantiteAttendue; }
    public void setQuantiteAttendue(double quantiteAttendue) { this.quantiteAttendue = quantiteAttendue; }

    public Map<Long, Double> getMatieresPremieres() { return matieresPremieres; }
    public void setMatieresPremieres(Map<Long, Double> matieresPremieres) { this.matieresPremieres = matieresPremieres; }
}
