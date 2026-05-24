package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class AppSettings {
    @Id
    private String cle;
    private String valeur;

    public AppSettings(String cle, String valeur) {
        this.cle = cle;
        this.valeur = valeur;
    }
}
