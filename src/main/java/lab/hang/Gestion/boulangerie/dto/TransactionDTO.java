package lab.hang.Gestion.boulangerie.dto;

import java.time.LocalDate;

public class TransactionDTO {

    private  Long id;
    private LocalDate date;
    private String type;
    private String description;
    private double montant;

    // Getters et Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }
}