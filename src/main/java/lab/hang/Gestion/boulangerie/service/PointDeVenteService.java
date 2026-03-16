package lab.hang.Gestion.boulangerie.service;

import jakarta.transaction.Transactional;
import lab.hang.Gestion.boulangerie.dto.GuichetDTO;
import lab.hang.Gestion.boulangerie.dto.PointDeVenteDTO;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.Guichet;
import lab.hang.Gestion.boulangerie.model.PointDeVente;
import lab.hang.Gestion.boulangerie.model.TypePointDeVente;
import lab.hang.Gestion.boulangerie.repository.CommandeRepository;
import lab.hang.Gestion.boulangerie.repository.GuichetRepository;
import lab.hang.Gestion.boulangerie.repository.PointDeVenteRepository;
import lab.hang.Gestion.boulangerie.repository.VenteLibreRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PointDeVenteService {

    private final PointDeVenteRepository pointDeVenteRepository;
    private final GuichetRepository guichetRepository;
    private final CommandeRepository commandeRepository;
    private final VenteLibreRepository venteLibreRepository;

    public PointDeVenteService(PointDeVenteRepository pointDeVenteRepository,
                               GuichetRepository guichetRepository,
                               CommandeRepository commandeRepository,
                               VenteLibreRepository venteLibreRepository) {
        this.pointDeVenteRepository = pointDeVenteRepository;
        this.guichetRepository = guichetRepository;
        this.commandeRepository = commandeRepository;
        this.venteLibreRepository = venteLibreRepository;
    }

    // ─── Points de vente ──────────────────────────────────────────────────────

    public List<PointDeVenteDTO> getAllPointsDeVente() {
        return pointDeVenteRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PointDeVenteDTO> getPointsDeVenteActifs() {
        return pointDeVenteRepository.findByActifTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PointDeVenteDTO getById(Long id) {
        PointDeVente pdv = pointDeVenteRepository.findByIdWithGuichets(id);
        if (pdv == null) throw new ResourceNotFoundException("Point de vente non trouvé : " + id);
        return toDTOWithGuichets(pdv);
    }

    public PointDeVente getEntityById(Long id) {
        return pointDeVenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Point de vente non trouvé : " + id));
    }

    @Transactional
    public PointDeVenteDTO creer(PointDeVenteDTO dto) {
        PointDeVente pdv = new PointDeVente();
        pdv.setNom(dto.getNom());
        pdv.setAdresse(dto.getAdresse());
        pdv.setType(dto.getType());
        pdv.setActif(true);
        return toDTO(pointDeVenteRepository.save(pdv));
    }

    @Transactional
    public PointDeVenteDTO modifier(Long id, PointDeVenteDTO dto) {
        PointDeVente pdv = getEntityById(id);
        pdv.setNom(dto.getNom());
        pdv.setAdresse(dto.getAdresse());
        pdv.setType(dto.getType());
        pdv.setActif(dto.isActif());
        return toDTO(pointDeVenteRepository.save(pdv));
    }

    @Transactional
    public void desactiver(Long id) {
        PointDeVente pdv = getEntityById(id);
        pdv.setActif(false);
        pointDeVenteRepository.save(pdv);
    }

    @Transactional
    public void supprimer(Long id) {
        pointDeVenteRepository.deleteById(id);
    }

    // ─── Guichets ─────────────────────────────────────────────────────────────

    public List<GuichetDTO> getGuichetsByPointDeVente(Long pointDeVenteId) {
        return guichetRepository.findByPointDeVenteId(pointDeVenteId).stream()
                .map(this::toGuichetDTO)
                .collect(Collectors.toList());
    }

    public List<GuichetDTO> getGuichetsActifs(Long pointDeVenteId) {
        return guichetRepository.findByPointDeVenteIdAndActifTrue(pointDeVenteId).stream()
                .map(this::toGuichetDTO)
                .collect(Collectors.toList());
    }

    public List<GuichetDTO> getAllGuichetsActifs() {
        return guichetRepository.findByActifTrue().stream()
                .map(this::toGuichetDTO)
                .collect(Collectors.toList());
    }

    public Guichet getGuichetEntityById(Long id) {
        return guichetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guichet non trouvé : " + id));
    }

    @Transactional
    public GuichetDTO creerGuichet(GuichetDTO dto) {
        PointDeVente pdv = getEntityById(dto.getPointDeVenteId());
        Guichet guichet = new Guichet();
        guichet.setNom(dto.getNom());
        guichet.setNumero(dto.getNumero());
        guichet.setPointDeVente(pdv);
        guichet.setActif(true);
        return toGuichetDTO(guichetRepository.save(guichet));
    }

    @Transactional
    public GuichetDTO modifierGuichet(Long id, GuichetDTO dto) {
        Guichet guichet = getGuichetEntityById(id);
        guichet.setNom(dto.getNom());
        guichet.setNumero(dto.getNumero());
        guichet.setActif(dto.isActif());
        return toGuichetDTO(guichetRepository.save(guichet));
    }

    @Transactional
    public void supprimerGuichet(Long id) {
        guichetRepository.deleteById(id);
    }

    // ─── Stats par point de vente ─────────────────────────────────────────────

    public PointDeVenteDTO getStatsPointDeVente(Long id) {
        PointDeVente pdv = getEntityById(id);
        PointDeVenteDTO dto = toDTOWithGuichets(pointDeVenteRepository.findByIdWithGuichets(id));

        // CA total issu des commandes liées à ce point de vente
        Double caCommandes = commandeRepository.sumCoutTotalByPointDeVenteId(id);
        // CA total issu des ventes libres liées aux guichets de ce point de vente
        Double caVentesLibres = venteLibreRepository.sumMontantTotalByGuichetPointDeVenteId(id);

        dto.setChiffreAffairesTotal((caCommandes != null ? caCommandes : 0)
                + (caVentesLibres != null ? caVentesLibres : 0));

        // CA du mois en cours
        LocalDate debut = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = LocalDate.now();
        Double caMoisCommandes = commandeRepository.sumCoutTotalByPointDeVenteIdAndDateBetween(id, debut, fin);
        Double caMoisVentes = venteLibreRepository.sumMontantTotalByGuichetPointDeVenteIdAndDateBetween(id, debut, fin);
        dto.setChiffreAffairesMois((caMoisCommandes != null ? caMoisCommandes : 0)
                + (caMoisVentes != null ? caMoisVentes : 0));

        // Nombre de ventes total
        long nbCommandes = commandeRepository.countByPointDeVenteId(id);
        long nbVentesLibres = venteLibreRepository.countByGuichetPointDeVenteId(id);
        dto.setNombreVentes(nbCommandes + nbVentesLibres);

        return dto;
    }

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private PointDeVenteDTO toDTO(PointDeVente pdv) {
        PointDeVenteDTO dto = new PointDeVenteDTO();
        dto.setId(pdv.getId());
        dto.setNom(pdv.getNom());
        dto.setAdresse(pdv.getAdresse());
        dto.setType(pdv.getType());
        dto.setActif(pdv.isActif());
        return dto;
    }

    private PointDeVenteDTO toDTOWithGuichets(PointDeVente pdv) {
        PointDeVenteDTO dto = toDTO(pdv);
        dto.setGuichets(pdv.getGuichets().stream()
                .map(this::toGuichetDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    public GuichetDTO toGuichetDTO(Guichet g) {
        GuichetDTO dto = new GuichetDTO();
        dto.setId(g.getId());
        dto.setNom(g.getNom());
        dto.setNumero(g.getNumero());
        dto.setActif(g.isActif());
        dto.setPointDeVenteId(g.getPointDeVente().getId());
        dto.setNomPointDeVente(g.getPointDeVente().getNom());
        return dto;
    }
}
