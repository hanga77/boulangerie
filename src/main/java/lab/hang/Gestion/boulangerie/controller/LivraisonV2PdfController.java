package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.application.sales.CatalogSalesPort;
import lab.hang.Gestion.boulangerie.domain.sales.LivraisonLigneV2;
import lab.hang.Gestion.boulangerie.domain.sales.LivraisonV2;
import lab.hang.Gestion.boulangerie.infrastructure.persistence.LivraisonV2Repository;
import lab.hang.Gestion.boulangerie.service.UserService;
import com.lowagie.text.pdf.BaseFont;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 3 P2 — PDF facture pour livraison V2.
 */
@Controller
public class LivraisonV2PdfController {

    private final LivraisonV2Repository livraisonRepo;
    private final CatalogSalesPort catalogPort;
    private final TemplateEngine templateEngine;
    private final UserService userService;

    @Value("${app.name:Gestiboul}")
    private String appName;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private volatile String fontFilePath;

    public LivraisonV2PdfController(LivraisonV2Repository livraisonRepo,
                                    CatalogSalesPort catalogPort,
                                    TemplateEngine templateEngine,
                                    UserService userService) {
        this.livraisonRepo = livraisonRepo;
        this.catalogPort = catalogPort;
        this.templateEngine = templateEngine;
        this.userService = userService;
    }

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

    @GetMapping("/cycle/livraison/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void pdf(@PathVariable Long id, HttpServletResponse response) throws Exception {
        LivraisonV2 livraison = livraisonRepo.findByIdWithLignes(id)
                .or(() -> livraisonRepo.findById(id))
                .orElseThrow(() -> new IllegalArgumentException("Livraison V2 introuvable: " + id));

        List<Map<String, Object>> lignes = new ArrayList<>();
        for (LivraisonLigneV2 l : livraison.getLignes()) {
            Map<String, Object> row = new HashMap<>();
            row.put("nom", catalogPort.nomProduit(l.getProduitId()));
            row.put("quantite", l.getQuantite());
            row.put("prixVente", l.getPrixVente());
            row.put("total", l.getQuantite() * l.getPrixVente());
            lignes.add(row);
        }

        Context context = new Context();
        context.setVariable("livraison", livraison);
        context.setVariable("lignes", lignes);
        context.setVariable("dateImpression", LocalDateTime.now());
        context.setVariable("user", userService.getCurrentUser());
        context.setVariable("appName", appName);
        File logo = new File(uploadDir, "logo.png").getAbsoluteFile();
        context.setVariable("customLogoUrl", logo.exists() ? logo.toURI().toString() : null);

        String html = templateEngine.process("cycle/facture-livraison-v2", context);

        ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.getFontResolver().addFont(resolveFontFile(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(pdfBytes);

        String filename = "facture-livraison-v2-" + id +
                (livraison.getFactureNumero() != null ? "-" + livraison.getFactureNumero() : "") + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        try (OutputStream out = response.getOutputStream()) {
            out.write(pdfBytes.toByteArray());
        }
    }
}
