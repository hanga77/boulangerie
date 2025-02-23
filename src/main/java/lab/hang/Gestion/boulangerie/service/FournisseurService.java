package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.exception.EntityNotFoundException;
import lab.hang.Gestion.boulangerie.model.Fournisseur;
import lab.hang.Gestion.boulangerie.repository.FournisseurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FournisseurService {
    private final FournisseurRepository fournisseurRepository;

    public FournisseurService(FournisseurRepository fournisseurRepository) {
        this.fournisseurRepository = fournisseurRepository;
    }

    public List<Fournisseur> getAllFournisseurs() {
        return fournisseurRepository.findAll();
    }

    public Fournisseur getFournisseurById(Long id) {
        return fournisseurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur non trouvé"));
    }

    @Transactional
    public Fournisseur saveFournisseur(Fournisseur fournisseur) {
        return fournisseurRepository.save(fournisseur);
    }
}
