package lab.hang.Gestion.boulangerie.application.sales;

public interface CatalogSalesPort {
    double prixProduit(Long produitId);
    String nomProduit(Long produitId);
}
