package lab.hang.Gestion.boulangerie.service;

import jakarta.transaction.Transactional;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.PointDeVente;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.model.Versement;
import lab.hang.Gestion.boulangerie.repository.LivraisonRepository;
import lab.hang.Gestion.boulangerie.repository.PointDeVenteRepository;
import lab.hang.Gestion.boulangerie.repository.VersementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class VersementService {

    private final VersementRepository versementRepository;
    private final LivraisonRepository livraisonRepository;
    private final PointDeVenteRepository pointDeVenteRepository;

    public VersementService(VersementRepository versementRepository,
                            LivraisonRepository livraisonRepository,
                            PointDeVenteRepository pointDeVenteRepository) {
        this.versementRepository = versementRepository;
        this.livraisonRepository = livraisonRepository;
        this.pointDeVenteRepository = pointDeVenteRepository;
    }

    /** Solde dû par le point de vente = total livré - total versé. */
    public double calculerSolde(Long pointDeVenteId) {
        double totalLivre = livraisonRepository.sumMontantByPointDeVenteId(pointDeVenteId);
        double totalVerse = versementRepository.sumMontantByPointDeVenteId(pointDeVenteId);
        return totalLivre - totalVerse;
    }

    public List<Versement> getVersementsByPointDeVente(Long pointDeVenteId) {
        return versementRepository.findByPointDeVenteIdOrderByDateDesc(pointDeVenteId);
    }

    public Versement enregistrerVersement(Long pointDeVenteId, double montant, String motif, User user) {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant du versement doit être positif.");
        }
        PointDeVente pointDeVente = pointDeVenteRepository.findById(pointDeVenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Point de vente non trouvé"));

        Versement versement = new Versement();
        versement.setPointDeVente(pointDeVente);
        versement.setMontant(montant);
        versement.setDate(LocalDate.now());
        versement.setMotif(motif);
        versement.setUser(user);
        return versementRepository.save(versement);
    }
}
