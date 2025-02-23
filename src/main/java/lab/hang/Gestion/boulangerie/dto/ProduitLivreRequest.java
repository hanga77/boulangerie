package lab.hang.Gestion.boulangerie.dto;

import lombok.Data;


public class ProduitLivreRequest {
    private int quantite;
    private double prixVente;

    // Getters and setters

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(double prixVente) {
        this.prixVente = prixVente;
    }
}
