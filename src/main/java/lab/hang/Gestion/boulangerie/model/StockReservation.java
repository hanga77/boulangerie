package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class StockReservation {
    @ManyToOne
    private MatierePremiere matierePremiere;

    private double quantite;
    private LocalDateTime dateReservation;
    private LocalDateTime dateExpiration;

    @Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    public MatierePremiere getMatierePremiere() {
        return matierePremiere;
    }

    public void setMatierePremiere(MatierePremiere matierePremiere) {
        this.matierePremiere = matierePremiere;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
