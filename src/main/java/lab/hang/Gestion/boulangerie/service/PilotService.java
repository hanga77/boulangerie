package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.model.PilotObservation;
import lab.hang.Gestion.boulangerie.repository.PilotObservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PilotService {

    public static final List<String> ETAPES = List.of(
            "COMMANDES",
            "PRODUCTION",
            "MAGASIN",
            "VALIDATION",
            "LIVRAISONS",
            "CAISSE",
            "FINANCE",
            "SYNTHESE",
            "CLOTURE",
            "AUTRE"
    );

    public static final Map<String, String> ETAPE_LABELS = Map.ofEntries(
            Map.entry("COMMANDES", "1. Commandes du jour"),
            Map.entry("PRODUCTION", "2. Lancer la production"),
            Map.entry("MAGASIN", "3. Sorties magasin / farinage"),
            Map.entry("VALIDATION", "4. Valider quantités produites"),
            Map.entry("LIVRAISONS", "5. Livraisons V2"),
            Map.entry("CAISSE", "6. Caisse V2"),
            Map.entry("FINANCE", "7. Dépenses / recettes"),
            Map.entry("SYNTHESE", "8. Synthèse / marge / trésorerie"),
            Map.entry("CLOTURE", "9. Clôture de journée"),
            Map.entry("AUTRE", "Autre / transversal")
    );

    private final PilotObservationRepository repository;

    public PilotService(PilotObservationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PilotObservation enregistrer(LocalDate dateJour, String etape, boolean ok,
                                        String commentaire, String auteur) {
        if (etape == null || etape.isBlank()) {
            throw new IllegalArgumentException("Étape obligatoire");
        }
        String code = etape.trim().toUpperCase();
        if (!ETAPES.contains(code)) {
            throw new IllegalArgumentException("Étape inconnue: " + etape);
        }
        PilotObservation o = new PilotObservation();
        o.setDateJour(dateJour != null ? dateJour : LocalDate.now());
        o.setEtape(code);
        o.setOk(ok);
        o.setCommentaire(commentaire != null ? commentaire.trim() : null);
        o.setAuteur(auteur);
        return repository.save(o);
    }

    @Transactional(readOnly = true)
    public List<PilotObservation> pourJour(LocalDate date) {
        return repository.findByDateJourOrderByCreatedAtDesc(date != null ? date : LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<PilotObservation> recentes() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> progression(LocalDate date) {
        Map<String, Boolean> map = new LinkedHashMap<>();
        for (String e : ETAPES) {
            if ("AUTRE".equals(e)) continue;
            map.put(e, false);
        }
        for (PilotObservation o : pourJour(date)) {
            if (map.containsKey(o.getEtape()) && o.isOk()) {
                map.put(o.getEtape(), true);
            }
        }
        return map;
    }
}
