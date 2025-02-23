package lab.hang.Gestion.boulangerie.model;



public class ProduitCommande {
    private Produit produit;
    private Integer quantite;

    // Getters and Setters
    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }
}