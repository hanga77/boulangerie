package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class ProduitLivre {
    private int quantite;
    private double prixInitial;
    private double prixVente;

    // Getters and setters


    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrixInitial() {
        return prixInitial;
    }

    public void setPrixInitial(double prixInitial) {
        this.prixInitial = prixInitial;
    }

    public double getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(double prixVente) {
        this.prixVente = prixVente;
    }
}