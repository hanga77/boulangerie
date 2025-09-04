package lab.hang.Gestion.boulangerie.service;

import jakarta.mail.internet.MimeMessage;
import lab.hang.Gestion.boulangerie.model.Facture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;



@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String expediteurEmail;

    public EmailService(JavaMailSender emailSender, TemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    public void envoyerRelanceFacture(Facture facture) {
        if (facture == null || facture.getClient() == null ) {
            throw new IllegalArgumentException("La facture ou l'e-mail du client est invalide");
        }

        try {
            Context context = new Context();
            context.setVariable("facture", facture);

            String emailContent = templateEngine.process("emails/relance-facture", context);

            jakarta.mail.internet.MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(facture.getClient().getEmail());
            helper.setFrom(expediteurEmail);
            helper.setSubject("Relance - Facture " + facture.getNumero());
            helper.setText(emailContent, true);

            // Ajouter une pièce jointe (exemple)
            // FileSystemResource file = new FileSystemResource(new File("chemin/vers/facture.pdf"));
            // helper.addAttachment("facture.pdf", file);

            emailSender.send(message);
            logger.info("E-mail de relance envoyé pour la facture {}", facture.getNumero());
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void envoyerEmailGeneral(String destinataire, String sujet, String contenu, boolean isHtml) {
        if (destinataire == null || destinataire.isEmpty()) {
            throw new IllegalArgumentException("L'adresse e-mail du destinataire ne peut pas être vide");
        }

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(destinataire);
            helper.setFrom(expediteurEmail);
            helper.setSubject(sujet);
            helper.setText(contenu, isHtml);

            emailSender.send(message);
            logger.info("E-mail envoyé à {}", destinataire);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}