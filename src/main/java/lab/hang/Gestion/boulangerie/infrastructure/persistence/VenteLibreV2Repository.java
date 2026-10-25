package lab.hang.Gestion.boulangerie.infrastructure.persistence;

import lab.hang.Gestion.boulangerie.domain.sales.VenteLibreV2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VenteLibreV2Repository extends JpaRepository<VenteLibreV2, Long> {
    List<VenteLibreV2> findByProductionV2Id(Long productionV2Id);
    int countByDateVente(LocalDate dateVente);

    List<VenteLibreV2> findByDateVenteBetweenOrderByDateVenteDescIdDesc(
            LocalDate debut, LocalDate fin);
}
