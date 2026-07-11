package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.dto.CommandeDTO;
import lab.hang.Gestion.boulangerie.dto.LivraisonDTO;
import lab.hang.Gestion.boulangerie.dto.ProductionDTO;
import lab.hang.Gestion.boulangerie.dto.ProduitDTO;
import lab.hang.Gestion.boulangerie.mapper.ProductionMapper;
import lab.hang.Gestion.boulangerie.model.BulletinDePaie;
import lab.hang.Gestion.boulangerie.model.ChargeFixe;
import lab.hang.Gestion.boulangerie.model.MatierePremiere;
import lab.hang.Gestion.boulangerie.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.lowagie.text.pdf.BaseFont;

import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PdfController {

    private static final Logger log = LoggerFactory.getLogger(PdfController.class);

    private final TemplateEngine templateEngine;
    private final CommandeService commandeService;
    private final ProductionService productionService;

    private final UserService userService;
    private final ProduitService produitService;

    private final ProductionMapper productionMapper;

    private final LivraisonService livraisonService;
    private final MatierePremiereService matierePremiereService;

    private final FinanceService financeService;
    private final ChargeFixeService chargeFixeService;
    private final FacturationService facturationService;

    private final KPIService kpiService;
    private final BulletinDePaieService bulletinDePaieService;
    private final EmployeService employeService;

    @Value("${app.name}")
    private String appName;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.documents.dir:documents}")
    private String documentsDir;

    // ClassPathResource.getFile() échoue quand l'app tourne depuis un JAR exécutable
    // (la ressource est nichée dans BOOT-INF/classes, pas sur le système de fichiers) —
    // iText a besoin d'un chemin réel, donc on extrait la police une seule fois vers un fichier temporaire.
    private volatile String fontFilePath;

    private String resolveFontFile() throws Exception {
        if (fontFilePath == null) {
            synchronized (this) {
                if (fontFilePath == null) {
                    File tempFont = File.createTempFile("gestiboul-arial-", ".ttf");
                    tempFont.deleteOnExit();
                    try (InputStream in = new ClassPathResource("static/fonts/arial.ttf").getInputStream()) {
                        Files.copy(in, tempFont.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    fontFilePath = tempFont.getAbsolutePath();
                }
            }
        }
        return fontFilePath;
    }

    public PdfController(TemplateEngine templateEngine, CommandeService commandeService, ProductionService productionService, UserService userService, ProduitService produitService, ProductionMapper productionMapper, LivraisonService livraisonService, MatierePremiereService matierePremiereService, FinanceService financeService, ChargeFixeService chargeFixeService, FacturationService facturationService, KPIService kpiService, BulletinDePaieService bulletinDePaieService, EmployeService employeService) {
        this.templateEngine = templateEngine;
        this.commandeService = commandeService;
        this.productionService = productionService;
        this.userService = userService;
        this.produitService = produitService;
        this.productionMapper = productionMapper;
        this.livraisonService = livraisonService;
        this.matierePremiereService = matierePremiereService;
        this.financeService = financeService;
        this.chargeFixeService = chargeFixeService;
        this.facturationService = facturationService;
        this.kpiService = kpiService;
        this.bulletinDePaieService = bulletinDePaieService;
        this.employeService = employeService;
    }

    private void addBrandToContext(Context context) {
        context.setVariable("appName", appName);
        File logo = new File(uploadDir, "logo.png").getAbsoluteFile();
        context.setVariable("customLogoUrl", logo.exists() ? logo.toURI().toString() : null);
    }

    /**
     * Rend un template en PDF, l'archive dans uploads/documents/{type}/{annee}/{mois}/,
     * puis le streame au client. L'échec de l'archivage n'empêche pas le téléchargement.
     */
    private void renderAndDeliverPdf(String templateName, Context context, HttpServletResponse response,
                                     String documentType, String filename) throws Exception {
        String html = templateEngine.process(templateName, context);

        ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.getFontResolver().addFont(
                resolveFontFile(),
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED
        );
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(pdfBytes);
        byte[] content = pdfBytes.toByteArray();

        archiverPdf(documentType, filename, content);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        try (OutputStream out = response.getOutputStream()) {
            out.write(content);
        }
    }

    private void archiverPdf(String documentType, String filename, byte[] content) {
        try {
            LocalDate today = LocalDate.now();
            File dir = new File(documentsDir, documentType + "/"
                    + today.getYear() + "/" + String.format("%02d", today.getMonthValue()));
            dir.mkdirs();
            Files.write(new File(dir, filename).toPath(), content);
        } catch (Exception e) {
            log.warn("Impossible d'archiver le PDF {} ({}) : {}", filename, documentType, e.getMessage());
        }
    }

    @GetMapping("/commande/imprimer")
    public void printCommandes(@RequestParam Long id, HttpServletResponse response) throws Exception {
        CommandeDTO commande = commandeService.getCommandeById(id);

        Context context = new Context();
        context.setVariable("commande", commande);

        Map<ProduitDTO, Integer> produitsAvecNoms = new HashMap<>();
        commande.getProduitsCommandes().forEach((produitId, quantite) -> {
            ProduitDTO produit = produitService.getProduitById(produitId);
            produitsAvecNoms.put(produit, quantite);
        });

        context.setVariable("produitsAvecNoms", produitsAvecNoms);
        context.setVariable("user", userService.getUserById(commande.getUserId()));
        context.setVariable("appName", appName);

        renderAndDeliverPdf("commandes/pdf-template", context, response,
                "commandes", "commande-" + id + ".pdf");
    }

    @GetMapping("production/print")
    public void printProductions(@RequestParam Long id, HttpServletResponse response) throws Exception {

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

        Context context = new Context();
        context.setVariable("production", productionDTO);
        context.setVariable("produitsMap", produitsMap);
        context.setVariable("matieresMap", matieresMap);
        context.setVariable("user", userService.getUserById(productionDTO.getUserId()));
        context.setVariable("appName", appName);

        renderAndDeliverPdf("production/print", context, response,
                "productions", "production-" + id + ".pdf");
    }

    @GetMapping("/livraisons/{id}/imprimer")
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
        addBrandToContext(context);

        renderAndDeliverPdf("livraisons/facture-template", context, response,
                "factures", "facture-livraison-" + id + ".pdf");
    }

    @GetMapping("/bulletins/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or @bulletinSecurity.isBulletinOwner(#id, authentication)")
    public void getBulletinPdf(@PathVariable Long id, HttpServletResponse response) throws Exception {
        BulletinDePaie bulletin = bulletinDePaieService.getById(id);

        Context context = new Context();
        context.setVariable("bulletin", bulletin);
        addBrandToContext(context);

        String filename = "bulletin-" + bulletin.getId()
                + "-" + bulletin.getPeriode().getYear()
                + "-" + bulletin.getPeriode().getMonthValue() + ".pdf";

        renderAndDeliverPdf("employes/bulletin-template", context, response, "bulletins", filename);
    }

    @GetMapping("/comptabilite/charges-fixes/{id}/recu")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void getRecuChargePdf(@PathVariable Long id, HttpServletResponse response) throws Exception {
        ChargeFixe charge = chargeFixeService.getByIdForPdf(id);

        if (!charge.isPaye()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Cette charge n'est pas encore payée — reçu indisponible.");
            return;
        }

        Context context = new Context();
        context.setVariable("charge", charge);
        addBrandToContext(context);

        String filename = "recu-charge-" + charge.getId() + "-" + charge.getDatePaiement() + ".pdf";

        renderAndDeliverPdf("comptabilite/recu-charge-template", context, response, "recus", filename);
    }

    @GetMapping("/rapport-financier")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void genererRapportFinancierMensuel(HttpServletResponse response) throws Exception {
        YearMonth moisActuel = YearMonth.now();
        LocalDate debutMois = moisActuel.atDay(1);
        LocalDate finMois = moisActuel.atEndOfMonth();

        double revenus = financeService.calculerRevenusTotaux(debutMois, finMois);
        double depenses = financeService.calculerDepensesTotales(debutMois, finMois);
        double salaires = financeService.calculerDepensesSalariales(debutMois, finMois);
        double coutProduction = productionService.calculerCoutTotalProduction(debutMois, finMois);
        double profit = revenus - (depenses + salaires + coutProduction);

        Context context = new Context();
        context.setVariable("mois", moisActuel);
        context.setVariable("debutMois", debutMois);
        context.setVariable("finMois", finMois);
        context.setVariable("revenus", revenus);
        context.setVariable("depenses", depenses);
        context.setVariable("salaires", salaires);
        context.setVariable("coutProduction", coutProduction);
        context.setVariable("profit", profit);
        context.setVariable("chargesFixes", chargeFixeService.getAllChargesFixe());
        context.setVariable("facturesImpayees", facturationService.getFacturesImpayees());
        context.setVariable("livraisons", livraisonService.getLivraisonsByDateRange(debutMois, finMois));
        context.setVariable("commandes", commandeService.getCommandesByDate(debutMois));
        context.setVariable("ventesProduits", productionService.getVentesParProduit(debutMois, finMois));
        context.setVariable("kpis", kpiService.getKPIsJournaliers());

        String htmlContent = templateEngine.process("rapport-financier", context);

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=rapport_financier_" + moisActuel + ".html");
        try (OutputStreamWriter writer = new OutputStreamWriter(
                response.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(htmlContent);
        }
    }

}