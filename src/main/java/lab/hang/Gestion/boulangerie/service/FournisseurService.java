package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.FournisseurDTO;
import lab.hang.Gestion.boulangerie.exception.EntityNotFoundException;
import lab.hang.Gestion.boulangerie.model.Fournisseur;
import lab.hang.Gestion.boulangerie.repository.FournisseurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional
    public void deleteFournisseur(Long id) {
        if (!fournisseurRepository.existsById(id)) {
            throw new EntityNotFoundException("Fournisseur non trouvé");
        }
        fournisseurRepository.deleteById(id);
    }

    public List<Fournisseur> searchFournisseursByNom(String nom) {
        return fournisseurRepository.findByNomContainingIgnoreCase(nom);
    }

    @Transactional
    public Fournisseur updateFournisseur(Long id, Fournisseur fournisseurDetails) {
        Fournisseur fournisseur = getFournisseurById(id);

        fournisseur.setNom(fournisseurDetails.getNom());
        fournisseur.setAdresse(fournisseurDetails.getAdresse());
        fournisseur.setTelephone(fournisseurDetails.getTelephone());
        fournisseur.setEmail(fournisseurDetails.getEmail());
        fournisseur.setReferenceContact(fournisseurDetails.getReferenceContact());
        fournisseur.setCategorieProduits(fournisseurDetails.getCategorieProduits());

        return fournisseurRepository.save(fournisseur);
    }

    public FournisseurDTO mapToDTO(Fournisseur fournisseur) {
        FournisseurDTO dto = new FournisseurDTO();
        dto.setId(fournisseur.getId());
        dto.setNom(fournisseur.getNom());
        dto.setAdresse(fournisseur.getAdresse());
        dto.setTelephone(fournisseur.getTelephone());
        dto.setEmail(fournisseur.getEmail());
        dto.setReferenceContact(fournisseur.getReferenceContact());
        dto.setCategorieProduits(fournisseur.getCategorieProduits());
        return dto;
    }

    public List<FournisseurDTO> getAllFournisseursDTO() {
        return getAllFournisseurs().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public FournisseurDTO getFournisseurDTOById(Long id) {
        return mapToDTO(getFournisseurById(id));
    }
}

