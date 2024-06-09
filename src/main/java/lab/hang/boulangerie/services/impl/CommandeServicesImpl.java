package lab.hang.boulangerie.services.impl;

import lab.hang.boulangerie.dto.CommandeDTO;
import lab.hang.boulangerie.dto.LigneCommandeDTO;

import lab.hang.boulangerie.entity.*;
import lab.hang.boulangerie.repository.CommandeRepository;
import lab.hang.boulangerie.repository.LigneCommandeRepository;
import lab.hang.boulangerie.services.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CommandeServicesImpl implements CommandeServices {
    private  final CommandeRepository commandeRepository;
    private  final LigneCommandeRepository ligneCommandeRepository;
    private final SessionServices sessionServices;
    private  final ProductsServices productsServices;
    private final MatiereServices matiereServices;
    private final StandardProductionServices standardProductionServices;
    @Override
    public void SaveCommand(CommandeDTO commandeDTO , List<LigneCommandeDTO> ligneCommandeDTO) {
        commandeDTO.setSession(sessionServices.getLastSessions());
        Commande commande = mapToCommande(commandeDTO);
        commandeRepository.save(commande);

        // Associer les lignes de commande à la commande
        for (LigneCommandeDTO ligneCommande : ligneCommandeDTO) {
            ligneCommande.setCommande(commande);
            ligneCommandeRepository.save(mapToLigneCommande(ligneCommande));
        }

    }

    @Override
    public String DeleteCommande(Commande commande) {
        commandeRepository.deleteById(commande.getCommandeID());
        return "Delete Success";
    }

    @Override
    public CommandeDTO UpdateCommande(CommandeDTO commandeDTO, List<LigneCommandeDTO> ligneCommande) {
        Commande commande = commandeRepository.findByCommandeID(commandeDTO.getCommandeID()).orElseThrow();
        for (LigneCommande ligneCommande1 : commande.getLigneCommandes()) {
            int index = commande.getLigneCommandes().indexOf(ligneCommande1);
            ligneCommande1.setCommande(commande);
            ligneCommande1.setQuantity(ligneCommande.get(index).getQuantity());
            ligneCommande1.setProductList(ligneCommande.get(index).getProductList());
            ligneCommandeRepository.save(ligneCommande1);
        }
        return mapToCommandeDTO(commande);
    }

    @Override
    public CommandeDTO getCommandeById(CommandeDTO commande) {
        return commandeRepository.findByCommandeID(commande.getCommandeID()).map(this::mapToCommandeDTO).orElseThrow();
    }

    @Override
    public List<CommandeDTO> getAllCommande(){
        return  commandeRepository.findAll(Sort.by(Sort.Direction.DESC,"dateCreated")).stream().map(this::mapToCommandeDTO).collect(Collectors.toList());
    }

    @Override
    public Map<Matiere, Integer> getMatiereCommande(){
        LocalDateTime debut = LocalDateTime.now();
        LocalDateTime fin =LocalDateTime.now().minusDays(1).withHour(23).withMinute(00).withSecond(00);
        List<Commande> commande = commandeRepository.findByDateCreatedBetweenAndSession(debut,fin,sessionServices.getLastSessions());

        return commande.stream().flatMap(
                c->c.getLigneCommandes().stream()
        ).flatMap(lc->lc.getProductList().getStandardProduction().getLigneStandards().stream())
                .collect(Collectors.toMap(
                        LigneStandard::getMatiere,
                        LigneStandard::getQuantity,
                        Integer::sum
                ));
    }
    @Override
    public Map<Products, Integer> getProduitCommande(){
        LocalDateTime debut = LocalDateTime.now();
        LocalDateTime fin =LocalDateTime.now().minusDays(1).withHour(23).withMinute(0).withSecond(0);
        List<Commande> commande = commandeRepository.findByDateCreatedBetweenAndSession(debut,fin,sessionServices.getLastSessions());

        return commande.stream().flatMap(p->p.getLigneCommandes().stream())
                .collect(Collectors.toMap(
                        LigneCommande::getProductList,
                        LigneCommande::getQuantity,
                        Integer::sum
                ));
    }

    private Commande mapToCommande(CommandeDTO commandeDTO){
        Commande commande = new Commande();
        commande.setCommandeID(commandeDTO.getCommandeID());
        commande.setSession(commandeDTO.getSession());
        commande.setPointVenteID(commandeDTO.getPointVenteID());
        commande.setDateCreated(commandeDTO.getDateCreated());
        commande.setMontantTotal(commandeDTO.getMontantTotal());
        commande.setLigneCommandes(commandeDTO.getLigneCommandes());
        return commande;
    }

    private CommandeDTO mapToCommandeDTO(Commande commande){
        CommandeDTO commandeDTO = new CommandeDTO();
        commandeDTO.setCommandeID(commande.getCommandeID());
        commandeDTO.setLigneCommandes(commande.getLigneCommandes());
        commandeDTO.setSession(commande.getSession());
        commandeDTO.setPointVenteID(commande.getPointVenteID());
        commandeDTO.setDateCreated(commande.getDateCreated());
        commandeDTO.setMontantTotal(commande.getMontantTotal());
        return commandeDTO;
    }

    private LigneCommande mapToLigneCommande(LigneCommandeDTO ligneCommandeDTO){
        LigneCommande ligneCommande = new LigneCommande();
        ligneCommande.setProductList(ligneCommandeDTO.getProductList());
        ligneCommande.setID(ligneCommandeDTO.getID());
        ligneCommande.setCommande(ligneCommandeDTO.getCommande());
        ligneCommande.setQuantity(ligneCommandeDTO.getQuantity());
        return ligneCommande;
    }
}
