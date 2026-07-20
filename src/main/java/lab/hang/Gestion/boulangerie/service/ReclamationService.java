package lab.hang.Gestion.boulangerie.service;

import jakarta.transaction.Transactional;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.PointDeVente;
import lab.hang.Gestion.boulangerie.model.Reclamation;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.PointDeVenteRepository;
import lab.hang.Gestion.boulangerie.repository.ReclamationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final PointDeVenteRepository pointDeVenteRepository;

    public ReclamationService(ReclamationRepository reclamationRepository,
                              PointDeVenteRepository pointDeVenteRepository) {
        this.reclamationRepository = reclamationRepository;
        this.pointDeVenteRepository = pointDeVenteRepository;
    }

    public Reclamation creer(Long pointDeVenteId, String message, User auteur) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Le message de la réclamation ne peut pas être vide.");
        }
        PointDeVente pointDeVente = pointDeVenteRepository.findById(pointDeVenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Point de vente non trouvé"));

        Reclamation reclamation = new Reclamation();
        reclamation.setPointDeVente(pointDeVente);
        reclamation.setAuteur(auteur);
        reclamation.setMessage(message);
        reclamation.setDateCreation(LocalDateTime.now());
        reclamation.setStatut("OUVERTE");
        return reclamationRepository.save(reclamation);
    }

    public List<Reclamation> getByPointDeVente(Long pointDeVenteId) {
        return reclamationRepository.findByPointDeVenteIdOrderByDateCreationDesc(pointDeVenteId);
    }

    public List<Reclamation> getAll() {
        return reclamationRepository.findAllByOrderByDateCreationDesc();
    }

    public Reclamation repondre(Long id, String reponse) {
        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réclamation non trouvée"));
        reclamation.setReponse(reponse);
        reclamation.setDateReponse(LocalDateTime.now());
        reclamation.setStatut("REPONDUE");
        return reclamationRepository.save(reclamation);
    }
}
