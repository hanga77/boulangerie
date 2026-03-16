package lab.hang.Gestion.boulangerie.service;

import jakarta.transaction.Transactional;
import lab.hang.Gestion.boulangerie.dto.CreateVenteLibreRequest;
import lab.hang.Gestion.boulangerie.dto.VenteLibreDTO;
import lab.hang.Gestion.boulangerie.exception.ResourceNotFoundException;
import lab.hang.Gestion.boulangerie.mapper.VenteLibreMapper;
import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class VenteLibreService {

    private final VenteLibreRepository venteLibreRepository;
    private final ProductionRepository productionRepository;
    private final ProduitRepository produitRepository;
    private final UserService userService;
    private final VenteLibreMapper venteLibreMapper;
    private final TransactionRepository transactionRepository;
    private final CompteBancaireRepository compteBancaireRepository;
    private final PointDeVenteService pointDeVenteService;

    public VenteLibreService(VenteLibreRepository venteLibreRepository,
                             ProductionRepository productionRepository,
                             ProduitRepository produitRepository,
                             UserService userService,
                             VenteLibreMapper venteLibreMapper,
                             TransactionRepository transactionRepository,
                             CompteBancaireRepository compteBancaireRepository,
                             PointDeVenteService pointDeVenteService) {
        this.venteLibreRepository = venteLibreRepository;
        this.productionRepository = productionRepository;
        this.produitRepository = produitRepository;
        this.userService = userService;
        this.venteLibreMapper = venteLibreMapper;
        this.transactionRepository = transactionRepository;
        this.compteBancaireRepository = compteBancaireRepository;
        this.pointDeVenteService = pointDeVenteService;
    }

    public VenteLibreDTO createVenteLibre(CreateVenteLibreRequest request) {
        User currentUser = userService.getCurrentUser();
        Production production = getAndValidateProduction(request.getProductionId());

        VenteLibre venteLibre = new VenteLibre();
        venteLibre.setDateVente(LocalDate.now());
        venteLibre.setProduction(production);
        venteLibre.setUser(currentUser);
        if (request.getGuichetId() != null) {
            venteLibre.setGuichet(pointDeVenteService.getGuichetEntityById(request.getGuichetId()));
        }

        Map<Produit, Integer> produitsVendus = new HashMap<>();
        Map<Produit, Integer> produitsRestants = production.getProduitsRestants();
        double montantTotal = 0;

        for (Map.Entry<Long, Integer> entry : request.getProduits().entrySet()) {
            int quantiteDemandee = entry.getValue();
            if (quantiteDemandee <= 0) continue;

            Produit produit = produitRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));

            int stockDisponible = produitsRestants.getOrDefault(produit, 0);
            if (stockDisponible < quantiteDemandee) {
                throw new IllegalArgumentException(
                        String.format("Stock insuffisant pour %s. Disponible: %d, Demandé: %d",
                                produit.getNom(), stockDisponible, quantiteDemandee));
            }

            produitsRestants.put(produit, stockDisponible - quantiteDemandee);
            produitsVendus.put(produit, quantiteDemandee);
            montantTotal += produit.getPrix() * quantiteDemandee;
        }

        venteLibre.setProduitsVendus(produitsVendus);
        venteLibre.setMontantTotal(montantTotal);
        production.setProduitsRestants(produitsRestants);

        VenteLibre saved = venteLibreRepository.save(venteLibre);
        productionRepository.save(production);

        enregistrerRevenu(saved.getId(), montantTotal);

        return venteLibreMapper.toDTO(saved);
    }

    private Production getAndValidateProduction(Long productionId) {
        Production production = productionRepository.findById(productionId)
                .orElseThrow(() -> new ResourceNotFoundException("Production non trouvée"));
        LocalDate today = LocalDate.now();
        if (!production.getDateProduction().equals(today) &&
                !production.getDateProduction().equals(today.minusDays(1))) {
            throw new IllegalArgumentException("La production doit être du jour même ou de la veille");
        }
        return production;
    }

    private void enregistrerRevenu(Long venteLibreId, double montantTotal) {
        CompteBancaire compte = compteBancaireRepository.findByNom("Compte Principal");
        compte.setSolde(compte.getSolde() + montantTotal);

        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setType("VENTE_LIBRE");
        transaction.setMontant(montantTotal);
        transaction.setDescription("Vente libre ID: " + venteLibreId);
        transaction.setCompteBancaire(compte);

        transactionRepository.save(transaction);
        compteBancaireRepository.save(compte);
    }

    public List<VenteLibreDTO> getVentesLibresByProduction(Long productionId) {
        return venteLibreRepository.findByProductionId(productionId)
                .stream().map(venteLibreMapper::toDTO).collect(Collectors.toList());
    }

    public List<VenteLibreDTO> getAllVentesLibres() {
        return venteLibreRepository.findAll()
                .stream().map(venteLibreMapper::toDTO).collect(Collectors.toList());
    }

    public VenteLibreDTO getVenteLibreById(Long id) {
        return venteLibreRepository.findById(id)
                .map(venteLibreMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Vente libre non trouvée"));
    }

    public List<VenteLibreDTO> getVentesLibresByDateRange(LocalDate debut, LocalDate fin) {
        return venteLibreRepository.findByDateVenteBetween(debut, fin)
                .stream().map(venteLibreMapper::toDTO).collect(Collectors.toList());
    }
}
