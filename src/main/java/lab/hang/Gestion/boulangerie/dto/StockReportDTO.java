package lab.hang.Gestion.boulangerie.dto;

import lab.hang.Gestion.boulangerie.model.MatierePremiere;

import java.time.LocalDate;
import java.util.Map;

public class StockReportDTO {
    private LocalDate date;
    private Map<MatierePremiere, StockSummary> stockSummary;

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Map<MatierePremiere, StockSummary> getStockSummary() {
        return stockSummary;
    }

    public void setStockSummary(Map<MatierePremiere, StockSummary> stockSummary) {
        this.stockSummary = stockSummary;
    }

    public static class StockSummary {
        private double initialStock;
        private double totalIn;
        private double totalOut;
        private double remainingStock;

        // Getters and Setters
        public double getInitialStock() {
            return initialStock;
        }

        public void setInitialStock(double initialStock) {
            this.initialStock = initialStock;
        }

        public double getTotalIn() {
            return totalIn;
        }

        public void setTotalIn(double totalIn) {
            this.totalIn = totalIn;
        }

        public double getTotalOut() {
            return totalOut;
        }

        public void setTotalOut(double totalOut) {
            this.totalOut = totalOut;
        }

        public double getRemainingStock() {
            return remainingStock;
        }

        public void setRemainingStock(double remainingStock) {
            this.remainingStock = remainingStock;
        }
    }
}