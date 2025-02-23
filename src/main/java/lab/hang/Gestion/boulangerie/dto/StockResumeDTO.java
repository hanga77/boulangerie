package lab.hang.Gestion.boulangerie.dto;

import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lombok.Data;


public class StockResumeDTO {
    private MatierePremiere matierePremiere;
    private double stockInitial;
    private double totalEntrees;
    private double totalSorties;
    private double stockRestant;

    // Getters et Setters

    public MatierePremiere getMatierePremiere() {
        return matierePremiere;
    }

    public void setMatierePremiere(MatierePremiere matierePremiere) {
        this.matierePremiere = matierePremiere;
    }

    public double getStockInitial() {
        return stockInitial;
    }

    public void setStockInitial(double stockInitial) {
        this.stockInitial = stockInitial;
    }

    public double getTotalEntrees() {
        return totalEntrees;
    }

    public void setTotalEntrees(double totalEntrees) {
        this.totalEntrees = totalEntrees;
    }

    public double getTotalSorties() {
        return totalSorties;
    }

    public void setTotalSorties(double totalSorties) {
        this.totalSorties = totalSorties;
    }

    public double getStockRestant() {
        return stockRestant;
    }

    public void setStockRestant(double stockRestant) {
        this.stockRestant = stockRestant;
    }
}
