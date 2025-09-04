package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.AlerteDTO;
import lab.hang.Gestion.boulangerie.model.Alerte;
import lab.hang.Gestion.boulangerie.repository.AlerteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlerteService {
    private final AlerteRepository alerteRepository;
    private final NotificationService notificationService;

    public AlerteService(AlerteRepository alerteRepository, NotificationService notificationService) {
        this.alerteRepository = alerteRepository;
        this.notificationService = notificationService;
    }

    public AlerteDTO creerAlerte(String type, String niveau, String message) {
        Alerte alerte = new Alerte();
        alerte.setType(type);
        alerte.setNiveau(niveau);
        alerte.setMessage(message);
        alerte.setDateCreation(LocalDate.now());
        alerte.setVue(false);

        Alerte saved = alerteRepository.save(alerte);

        // Envoyer notification si critique
        if ("CRITICAL".equals(niveau)) {
            notificationService.envoyerNotificationUrgente(message);
        }

        return mapToDTO(saved);
    }

    private AlerteDTO mapToDTO(Alerte alerte) {
        AlerteDTO alerteDTO = new AlerteDTO();
        alerteDTO.setId(alerte.getId());
        alerteDTO.setType(alerte.getType());
        alerteDTO.setNiveau(alerte.getNiveau());
        alerteDTO.setMessage(alerte.getMessage());
        alerteDTO.setDateCreation(alerte.getDateCreation());
        alerteDTO.setVue(alerte.isVue());
        return alerteDTO;
    }

    public List<AlerteDTO> getAlertesNonVues() {
        List<Alerte> alertes = alerteRepository.findByVueFalseOrderByDateCreationDesc();
        return alertes.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AlerteDTO> getAllAlertes() {
        List<Alerte> alertes = alerteRepository.findAllByOrderByDateCreationDesc();
        return alertes.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void marquerCommeVue(Long id) {
        Alerte alerte = alerteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée avec l'ID : " + id));
        alerte.setVue(true);
        alerteRepository.save(alerte);
    }

    public List<AlerteDTO> getAlertesByType(String type) {
        List<Alerte> alertes = alerteRepository.findByType(type);
        return alertes.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AlerteDTO> getAlertesByNiveau(String niveau) {
        List<Alerte> alertes = alerteRepository.findByNiveau(niveau);
        return alertes.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void supprimerAlerte(Long id) {
        alerteRepository.deleteById(id);
    }
}