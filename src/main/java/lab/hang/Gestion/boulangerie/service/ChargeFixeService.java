package lab.hang.Gestion.boulangerie.service;

import jakarta.transaction.Transactional;
import lab.hang.Gestion.boulangerie.dto.BilanChargesFixesDTO;
import lab.hang.Gestion.boulangerie.dto.ChargeFixeDTO;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.model.ChargeFixe;
import lab.hang.Gestion.boulangerie.repository.ChargeFixeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChargeFixeService {
    private final ChargeFixeRepository chargeFixeRepository;
    //private final ComptabiliteService comptabiliteService;
    private final AlerteService alerteService;

    public ChargeFixeService(ChargeFixeRepository chargeFixeRepository,
                             //ComptabiliteService comptabiliteService,
                             AlerteService alerteService) {
        this.chargeFixeRepository = chargeFixeRepository;
       // this.comptabiliteService = comptabiliteService;
        this.alerteService = alerteService;
    }

    public List<ChargeFixeDTO> getChargesFixesAVenir(int joursAvant) {
        LocalDate dateDebut = LocalDate.now();
        LocalDate dateFin = dateDebut.plusDays(joursAvant);
        return chargeFixeRepository.findByDateEcheanceBetween(dateDebut, dateFin)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ChargeFixeDTO mapToDTO(ChargeFixe chargeFixe) {
        ChargeFixeDTO chargeFixeDTO = new ChargeFixeDTO();
        chargeFixeDTO.setId(chargeFixe.getId());
        chargeFixeDTO.setType(chargeFixe.getType());
        chargeFixeDTO.setMontant(chargeFixe.getMontant());
        chargeFixeDTO.setDescription(chargeFixe.getDescription());
        chargeFixeDTO.setPeriodicite(chargeFixe.getPeriodicite());
        chargeFixeDTO.setDateEcheance(chargeFixe.getDateEcheance());
        chargeFixeDTO.setPaye(chargeFixe.isPaye());
        return chargeFixeDTO;
    }

    private ChargeFixe mapToEntity(ChargeFixeDTO dto) {
        ChargeFixe chargeFixe = new ChargeFixe();
        chargeFixe.setType(dto.getType());
        chargeFixe.setMontant(dto.getMontant());
        chargeFixe.setDescription(dto.getDescription());
        chargeFixe.setPeriodicite(dto.getPeriodicite());
        chargeFixe.setDateEcheance(dto.getDateEcheance());
        chargeFixe.setPaye(dto.isPaye());
        return chargeFixe;
    }


    @Transactional
    public void payerChargeFixe(Long id) {
        ChargeFixe charge = chargeFixeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charge fixe non trouvée"));

        charge.setPaye(true);
        //comptabiliteService.enregistrerPaiement(charge);

        // Créer la prochaine échéance si périodique
        if (charge.getPeriodicite() != null) {
            creerProchaineEcheance(charge);
        }

        chargeFixeRepository.save(charge);
    }

    private void creerProchaineEcheance(ChargeFixe charge) {
        ChargeFixe nouvelleCharge = new ChargeFixe();
        nouvelleCharge.setType(charge.getType());
        nouvelleCharge.setMontant(charge.getMontant());
        nouvelleCharge.setDescription(charge.getDescription());
        nouvelleCharge.setPeriodicite(charge.getPeriodicite());

        // Calculer prochaine date d'échéance
        LocalDate prochaineEcheance = switch (charge.getPeriodicite()) {
            case "MENSUEL" -> charge.getDateEcheance().plusMonths(1);
            case "TRIMESTRIEL" -> charge.getDateEcheance().plusMonths(3);
            case "ANNUEL" -> charge.getDateEcheance().plusYears(1);
            default -> null;
        };

        if (prochaineEcheance != null) {
            nouvelleCharge.setDateEcheance(prochaineEcheance);
            nouvelleCharge.setPaye(false);
            chargeFixeRepository.save(nouvelleCharge);

            // Créer une alerte pour nouvelle échéance
            alerteService.creerAlerte(
                    "CHARGE_FIXE",
                    "INFO",
                    "Nouvelle échéance créée pour " + charge.getType() + " le " + prochaineEcheance
            );
        }
    }


    @Transactional
    public void creerChargeFixe(ChargeFixeDTO chargeFixeDTO) {
        chargeFixeRepository.save(mapToEntity(chargeFixeDTO));
    }

    public List<ChargeFixeDTO> getAllChargesFixe() {
        return chargeFixeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ChargeFixeDTO> getBilanChargesFixes() {
        return chargeFixeRepository.findAll().stream()
                .filter(charge -> charge.isPaye())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

}