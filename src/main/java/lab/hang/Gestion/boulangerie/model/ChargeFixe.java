package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class ChargeFixe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // LOYER, SALAIRE, ELECTRICITE, EAU, ASSURANCE, etc.
    private double montant;
    private String description;
    private LocalDate dateEcheance;
    private boolean paye;
    private String periodicite; // MENSUEL, TRIMESTRIEL, ANNUEL

    @ManyToOne
    private CompteBancaire compteBancaire;

    // Getters et setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public boolean isPaye() {
        return paye;
    }

    public void setPaye(boolean paye) {
        this.paye = paye;
    }

    public String getPeriodicite() {
        return periodicite;
    }

    public void setPeriodicite(String periodicite) {
        this.periodicite = periodicite;
    }

    public CompteBancaire getCompteBancaire() {
        return compteBancaire;
    }

    public void setCompteBancaire(CompteBancaire compteBancaire) {
        this.compteBancaire = compteBancaire;
    }
}
