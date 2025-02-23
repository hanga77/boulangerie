package lab.hang.Gestion.boulangerie.service;

import lab.hang.Gestion.boulangerie.dto.CommandeDTO;
import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.exception.CommandeNotFoundException;
import lab.hang.Gestion.boulangerie.mapper.CommandeMapper;
import lab.hang.Gestion.boulangerie.model.Commande;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.model.Produit;
import lab.hang.Gestion.boulangerie.model.User;
import lab.hang.Gestion.boulangerie.repository.CommandeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final CommandeMapper commandeMapper;
    private final ProduitService produitService;
    private final UserService userService;

    public CommandeService(CommandeRepository commandeRepository, CommandeMapper commandeMapper, ProduitService produitService, UserService userService) {
        this.commandeRepository = commandeRepository;
        this.commandeMapper = commandeMapper;
        this.produitService = produitService;
        this.userService = userService;
    }

    // Créer une commande et retourner un CommandeDTO
    public CommandeDTO createCommande(CommandeDTO commandeDTO) {
        // Récupérer l'utilisateur connecté
        User currentUser = userService.getCurrentUser();
        Commande commande = commandeMapper.toEntity(commandeDTO);
        commande.setUser(currentUser);
        Commande savedCommande = commandeRepository.save(commande);
        return commandeMapper.toDTO(savedCommande);
    }

    // Récupérer toutes les commandes sous forme de CommandeDTO
    public Page<CommandeDTO> getAllCommandes(Pageable pageable) {
        return commandeRepository.findAll(pageable)
                .map(commandeMapper::toDTO);
    }

    // Récupérer une commande par son ID sous forme de CommandeDTO
    public CommandeDTO getCommandeById(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new CommandeNotFoundException("Commande non trouvée avec l'ID : " + id));
        return commandeMapper.toDTO(commande);
    }

    // Mettre à jour une commande et retourner un CommandeDTO
    public CommandeDTO updateCommande(Long id, CommandeDTO commandeDTO) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new CommandeNotFoundException("Commande non trouvée avec l'ID : " + id));

        // Mettre à jour les champs de la commande
        commande.setDateCommande(commandeDTO.getDateCommande());
        commande.setPointDeVente(commandeDTO.getPointDeVente());


        final double[] coutTotal = {0.0};

        // Convertir Map<Long, Integer> en Map<Produit, Integer>
        Map<Produit, Integer> produitsCommandes = new HashMap<>();
        commandeDTO.getProduitsCommandes().forEach((produitId, quantite) -> {
            Produit produit = produitService.getProduitEntityById(produitId); // Retourne une entité Produit
            produitsCommandes.put(produit, quantite);
            coutTotal[0] += produit.getPrix() * quantite;
        });
        commande.setProduitsCommandes(produitsCommandes);
        commande.setCoutTotal(coutTotal[0]);

        Commande updatedCommande = commandeRepository.save(commande);
        return commandeMapper.toDTO(updatedCommande);
    }

    // Supprimer une commande
    public void deleteCommande(Long id) {
        if (!commandeRepository.existsById(id)) {
            throw new CommandeNotFoundException("Commande non trouvée avec l'ID : " + id);
        }
        commandeRepository.deleteById(id);
    }

    // Calculer les matières premières nécessaires pour une date donnée
    public Map<MatierePremiere, Double> calculerMatieresPremieresPourDate(LocalDate date) {
        List<Commande> commandes = commandeRepository.findByDateCommandeAndProcessed(date,false);
        Map<MatierePremiere, Double> matieresNecessaires = new HashMap<>();

        for (Commande commande : commandes) {
            for (Map.Entry<Produit, Integer> produitEntry : commande.getProduitsCommandes().entrySet()) {
                Produit produit = produitEntry.getKey();
                int quantiteCommandee = produitEntry.getValue();

                // Convertir Produit en ProduitDTO
                ProduitDTO produitDTO = produitService.getProduitById(produit.getId()); // Retourne un ProduitDTO

                // Calculer les matières premières nécessaires
                Map<MatierePremiere, Double> matieresPourProduit = produitService.calculateMatieresPremieresNecessaires(produitDTO, quantiteCommandee);

                // Ajouter les quantités à la map globale
                matieresPourProduit.forEach((matiere, quantite) -> {
                    matieresNecessaires.merge(matiere, quantite, Double::sum);
                });
            }
        }

        return matieresNecessaires;
    }

    // Récupérer les commandes non traitées sous forme de CommandeDTO
    public List<CommandeDTO> getCommandesNonTraitees() {
        return commandeRepository.findByProcessedFalse().stream()
                .map(commandeMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Vérifier s'il existe des commandes pour une date donnée
    public boolean hasCommandeForDate(LocalDate date) {
        return commandeRepository.existsByDateCommande(date);
    }

    // Vérifier s'il existe des commandes pour aujourd'hui
    public boolean hasCommandeForToday() {
        LocalDate today = LocalDate.now();
        return commandeRepository.countByDateCommande(today) > 0;
    }

    // Récupérer le nombre de commandes pour une date donnée
    /*public int getNombreCommandesPourDate(LocalDate date) {
        return commandeRepository.countByDateCommande(date);
    }*/

    public int getNombreCommandesPourDate(LocalDate date) {
        long count =  commandeRepository.countByDateCommande(date);
        return (int) count; // Gérer le cas où count est null
    }
    // Récupérer le coût total des commandes pour une date donnée
    public double getCoutTotalPourDate(LocalDate date) {
        Double coutTotal = commandeRepository.sumCoutTotalByDateCommande(date);
        return coutTotal != null ? coutTotal : 0.0;
    }

    // Récupérer le nombre de points de vente actifs pour une date donnée
   /* public int getNombrePointsDeVenteActifsPourDate(LocalDate date) {
        return commandeRepository.countDistinctPointsDeVenteByDateCommande(date);
    }*/

    public int getNombrePointsDeVenteActifsPourDate(LocalDate date) {
        Long count = commandeRepository.countDistinctPointDeVenteByDateCommande(date);
        return count != null ? count.intValue() : 0; // Gérer le cas où count est null
    }
    // Vérifier si toutes les commandes pour aujourd'hui sont traitées
    public boolean areAllCommandesProcessedForToday() {
        LocalDate today = LocalDate.now();
        List<Commande> commandesDuJour = commandeRepository.findByDateCommande(today);
        return commandesDuJour.stream().allMatch(Commande::isProcessed);
    }

    // Récupérer les commandes pour une date donnée sous forme de CommandeDTO
    public List<CommandeDTO> getCommandesByDate(LocalDate date) {
        return commandeRepository.findByDateCommande(date).stream()
                .map(commandeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<CommandeDTO> getCommandesByDateAndEtat(LocalDate dateProduction) {
        return commandeRepository.findByDateCommandeAndProcessed(dateProduction, false).stream()
                .map(commandeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<CommandeDTO> getCommandesByProductionId(Long productionId) {
        List<Commande> commandes = commandeRepository.findByProductionId(productionId);
        return commandes.stream()
                .map(commandeMapper::toDTO)
                .collect(Collectors.toList());
    }
}