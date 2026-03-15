package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.StockReportDTO;
import lab.hang.Gestion.boulangerie.exception.MatierePremiereNotFoundException;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.StockMovement;
import lab.hang.Gestion.boulangerie.repository.MatierePremiereRepository;
import lab.hang.Gestion.boulangerie.repository.StockMovementRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

public class MatierePremiereService {

    private final MatierePremiereRepository matierePremiereRepository;
    private final StockMovementRepository stockMovementRepository;

    public MatierePremiereService(MatierePremiereRepository matierePremiereRepository, StockMovementRepository stockMovementRepository) {
        this.matierePremiereRepository = matierePremiereRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    @CacheEvict(value = "matieres-premieres", allEntries = true)
    public MatierePremiere saveMatierePremiere(MatierePremiere matierePremiere) {
        matierePremiere.setPrixUnitaire(0.0);
        return matierePremiereRepository.save(matierePremiere);
    }

    @Cacheable("matieres-premieres")
    public List<MatierePremiere> getAllMatierePremieres() {
        return matierePremiereRepository.findAll();
    }
    public Page<MatierePremiere> getAllMatierePremieres(Pageable pageable) {
        return matierePremiereRepository.findAll(pageable);
    }

    public MatierePremiere getMatierePremiereById(Long id) {
        return matierePremiereRepository.findById(id)
                .orElseThrow(() -> new MatierePremiereNotFoundException("Matière première non trouvée avec l'ID : " + id));
    }

    @Transactional
    @CacheEvict(value = "matieres-premieres", allEntries = true)
    public MatierePremiere updateMatierePremiere(Long id, MatierePremiere matierePremiereDetails) {
        MatierePremiere matierePremiere = getMatierePremiereById(id);
        matierePremiere.setNom(matierePremiereDetails.getNom());
        matierePremiere.setUniteMesure(matierePremiereDetails.getUniteMesure());
        matierePremiere.setStockMinimum(matierePremiereDetails.getStockMinimum());
        return matierePremiereRepository.save(matierePremiere);
    }


    @Transactional
    @CacheEvict(value = "matieres-premieres", allEntries = true)
    public void deleteMatierePremiere(Long id) {
        matierePremiereRepository.deleteById(id);
    }

    public List<MatierePremiere> searchMatierePremieres(String nom) {
        return matierePremiereRepository.findByNomContainingIgnoreCase(nom);
    }



    public boolean verifierStocks(Map<MatierePremiere, Double> matieresNecessaires) {
        for (Map.Entry<MatierePremiere, Double> entry : matieresNecessaires.entrySet()) {
            MatierePremiere matiere = entry.getKey();
            double quantiteNecessaire = entry.getValue();

            if (matiere.getStock() < quantiteNecessaire) {
                return false; // Stock insuffisant
            }
        }
        return true; // Stocks suffisants
    }

    public List<StockMovement> getStockMovementsByDate(LocalDate date) {
        return stockMovementRepository.findByDate(date);
    }

    public StockReportDTO generateStockReport(LocalDate date) {
        List<StockMovement> movements = getStockMovementsByDate(date);
        Map<MatierePremiere, StockReportDTO.StockSummary> stockSummary = new HashMap<>();

        for (StockMovement movement : movements) {
            MatierePremiere matiere = movement.getMatierePremiere();
            StockReportDTO.StockSummary summary = stockSummary.computeIfAbsent(matiere, k -> new StockReportDTO.StockSummary());

            if (movement.getType().equals("ENTREE")) {
                summary.setTotalIn(summary.getTotalIn() + movement.getQuantite());
            } else if (movement.getType().equals("SORTIE")) {
                summary.setTotalOut(summary.getTotalOut() + movement.getQuantite());
            }
        }

        for (Map.Entry<MatierePremiere, StockReportDTO.StockSummary> entry : stockSummary.entrySet()) {
            MatierePremiere matiere = entry.getKey();
            StockReportDTO.StockSummary summary = entry.getValue();
            summary.setInitialStock(matiere.getStock() - summary.getTotalIn() + summary.getTotalOut());
            summary.setRemainingStock(matiere.getStock());
        }

        StockReportDTO report = new StockReportDTO();
        report.setDate(date);
        report.setStockSummary(stockSummary);

        return report;
    }
}