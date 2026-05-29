package lab.hang.Gestion.boulangerie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "license_config")
public class LicenseConfig {

    @Id
    private Long id = 1L;

    /** Date de première installation — début du décompte démo 90 jours. */
    @Column(nullable = false)
    private LocalDate installedAt;

    /** DEMO ou FULL */
    @Column(nullable = false)
    private String type = "DEMO";

    /** Date d'activation (null si jamais activé). */
    private LocalDate activatedAt;
}
