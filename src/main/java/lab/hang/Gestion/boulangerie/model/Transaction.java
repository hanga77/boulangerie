package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String type; // "VENTE", "ACHAT", "PRODUCTION", etc.

    @Column(nullable = false)
    private double montant;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "compte_bancaire_id", nullable = false)
    private CompteBancaire compteBancaire;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public CompteBancaire getCompteBancaire() { return compteBancaire; }
    public void setCompteBancaire(CompteBancaire compteBancaire) { this.compteBancaire = compteBancaire; }
}

