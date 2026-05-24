package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.IncidentProductionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class IncidentProductionService {

    private final IncidentProductionRepository incidentRepository;
    private final StockService stockService;

    public IncidentProductionService(IncidentProductionRepository incidentRepository,
                                     StockService stockService) {
        this.incidentRepository = incidentRepository;
        this.stockService = stockService;
    }

    @Transactional
    public IncidentProduction creerIncident(Production production,
                                            TypeIncident type,
                                            Produit produitConcerne,
                                            MatierePremiere matiereConcernee,
                                            double quantitePerdue,
                                            String cause,
                                            User signaledBy) {
        IncidentProduction incident = new IncidentProduction();
        incident.setProduction(production);
        incident.setType(type);
        incident.setProduitConcerne(produitConcerne);
        incident.setMatiereConcernee(matiereConcernee);
        incident.setQuantitePerdue(quantitePerdue);
        incident.setCause(cause);
        incident.setDateIncident(production.getDateProduction());
        incident.setSignaledBy(signaledBy);
        incident.setStockAjuste(false);

        if (type == TypeIncident.MATIERE_AVARIEE && matiereConcernee != null) {
            StockMovement movement = stockService.lostStock(
                    matiereConcernee.getId(), quantitePerdue, "AVARIE: " + cause);
            incident.setMouvementStock(movement);
            incident.setStockAjuste(true);
        }

        return incidentRepository.save(incident);
    }

    public List<IncidentProduction> getIncidentsByProduction(Long productionId) {
        return incidentRepository.findByProductionId(productionId);
    }

    public List<IncidentProduction> getAllIncidents(LocalDate debut, LocalDate fin, TypeIncident type) {
        if (type != null) {
            return incidentRepository.findByDateIncidentBetweenAndType(debut, fin, type);
        }
        return incidentRepository.findByDateIncidentBetween(debut, fin);
    }
}
