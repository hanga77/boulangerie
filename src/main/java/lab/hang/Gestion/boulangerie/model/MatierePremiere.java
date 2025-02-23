package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;



@Entity
public class MatierePremiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Assurez-vous que cet attribut existe

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String uniteMesure;

    @Column(nullable = false)
    private double stock;

    @Column
    private double stockMinimum;

    @Column
    private double prixUnitaire = 0.0;


    // Méthode pour vérifier si le stock est critique
    public boolean isStockCritique() {
        return stock <= stockMinimum;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getStockMinimum() {
        return stockMinimum;
    }

    public void setStockMinimum(double stockMinimum) {
        this.stockMinimum = stockMinimum;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getUniteMesure() {
        return uniteMesure;
    }

    public void setUniteMesure(String uniteMesure) {
        this.uniteMesure = uniteMesure;
    }

    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

}