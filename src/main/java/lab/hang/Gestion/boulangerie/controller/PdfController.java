package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.CommandeDTO;
import lab.hang.Gestion.boulangerie.dto.LivraisonDTO;
import lab.hang.Gestion.boulangerie.dto.ProductionDTO;
import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.mapper.ProductionMapper;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.lowagie.text.pdf.BaseFont;

import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PdfController {

    private final TemplateEngine templateEngine;
    private final CommandeService commandeService;
    private final ProductionService productionService;

    private final UserService userService;
    private final ProduitService produitService;

    private final ProductionMapper productionMapper;

    private final LivraisonService livraisonService;
    private final MatierePremiereService matierePremiereService;

    @Value("${app.name}")
    private String appName;

    public PdfController(TemplateEngine templateEngine, CommandeService commandeService, ProductionService productionService, UserService userService, ProduitService produitService, ProductionMapper productionMapper, LivraisonService livraisonService, MatierePremiereService matierePremiereService) {
        this.templateEngine = templateEngine;
        this.commandeService = commandeService;
        this.productionService = productionService;
        this.userService = userService;
        this.produitService = produitService;
        this.productionMapper = productionMapper;
        this.livraisonService = livraisonService;
        this.matierePremiereService = matierePremiereService;
    }

    @GetMapping("/commande/imprimer")
    public void printCommandes(@RequestParam Long id, HttpServletResponse response) throws Exception {
        // Récupérer la commande
        CommandeDTO commande = commandeService.getCommandeById(id);

        // Préparer le contexte Thymeleaf
        Context context = new Context();
        context.setVariable("commande", commande);

        CommandeDTO commandeDTO = commandeService.getCommandeById(id);
        Map<ProduitDTO, Integer> produitsAvecNoms = new HashMap<>();

        // Convertir la Map<Long, Integer> en Map<ProduitDTO, Integer>
        commandeDTO.getProduitsCommandes().forEach((produitId, quantite) -> {
            ProduitDTO produit = produitService.getProduitById(produitId);
            produitsAvecNoms.put(produit, quantite);
        });

        context.setVariable("user", userService.getUserById(commande.getUserId()));
        context.setVariable("produitsAvecNoms",produitsAvecNoms );
        context.setVariable("appName", appName);

        // Générer le HTML à partir du template
        String html = templateEngine.process("commandes/pdf-template", context);

        // Configurer la réponse HTTP
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=commande-details.pdf");

        // Convertir le HTML en PDF
        try (OutputStream outputStream = response.getOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            // Charger la police en utilisant ClassPathResource
            ClassPathResource fontResource = new ClassPathResource("static/fonts/arial.ttf");
            renderer.getFontResolver().addFont(
                    fontResource.getFile().getAbsolutePath(),
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );

            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    @GetMapping("production/print")
    public void printProductions(@RequestParam Long id, HttpServletResponse response) {

        //recuperation de la production
        ProductionDTO productionDTO = productionService.getProductionById(id);
        Map<Long, ProduitDTO> produitsMap = new HashMap<>();
        for (Long produitId : productionDTO.getProduitsProduits().keySet()) {
            produitsMap.put(produitId, produitService.getProduitById(produitId));
        }

        Map<Long, MatierePremiere> matieresMap = new HashMap<>();
        for (Long matiereId : productionDTO.getMatieresPremieresUtilisees().keySet()) {
            matieresMap.put(matiereId, matierePremiereService.getMatierePremiereById(matiereId));
        }

        // Préparer le contexte Thymeleaf
        Context context = new Context();

        context.setVariable("production", productionDTO);
        context.setVariable("user", userService.getUserById(productionDTO.getUserId()));
        context.setVariable("appName", appName);

        // Générer le HTML à partir du template
        String html = templateEngine.process("production/print", context);

        // Configurer la réponse HTTP
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=commande-details.pdf");

        // Convertir le HTML en PDF
        try (OutputStream outputStream = response.getOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            // Charger la police en utilisant ClassPathResource
            ClassPathResource fontResource = new ClassPathResource("static/fonts/arial.ttf");
            renderer.getFontResolver().addFont(
                    fontResource.getFile().getAbsolutePath(),
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );

            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    @GetMapping("livraisons/{id}/imprimer")
    public void generateFacture(@PathVariable Long id, HttpServletResponse response) throws Exception {
        LivraisonDTO livraison = livraisonService.getLivraisonById(id);

        // Préparer le contexte Thymeleaf
        Context context = new Context();
        context.setVariable("livraison", livraison);
        context.setVariable("dateImpression", LocalDateTime.now());
        context.setVariable("user", userService.getCurrentUser());

        // Convertir les produits pour un affichage plus facile
        Map<String, Object> produitsDetails = new HashMap<>();
        livraison.getProduitsLivres().forEach((produitId, details) -> {
            ProduitDTO produit = produitService.getProduitById(produitId);
            Map<String, Object> detailsMap = new HashMap<>();
            detailsMap.put("nom", produit.getNom());
            detailsMap.put("quantite", details.getQuantite());
            detailsMap.put("prixInitial", details.getPrixInitial());
            detailsMap.put("prixVente", details.getPrixVente());
            detailsMap.put("total", details.getPrixVente() * details.getQuantite());
            produitsDetails.put(produitId.toString(), detailsMap);
        });
        context.setVariable("produitsDetails", produitsDetails);

        // Générer le HTML à partir du template
        String html = templateEngine.process("livraisons/facture-template", context);

        // Configurer la réponse HTTP
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=facture-livraison-" + id + ".pdf");

        // Convertir en PDF
        try (OutputStream outputStream = response.getOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            ClassPathResource fontResource = new ClassPathResource("static/fonts/arial.ttf");
            renderer.getFontResolver().addFont(
                    fontResource.getFile().getAbsolutePath(),
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );

            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
        }
    }

}