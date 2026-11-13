package lab.hang.Gestion.boulangerie.service;

import jakarta.mail.internet.MimeMessage;
import lab.hang.Gestion.boulangerie.model.Facture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Value("${spring.mail.username:}")
    private String expediteurEmail;

    public EmailService(@Autowired(required = false) JavaMailSender emailSender,
                        TemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    private void ensureMailAvailable() {
        if (emailSender == null) {
            throw new IllegalStateException(
                    "Envoi d'e-mail impossible : JavaMailSender non configuré " +
                    "(définissez spring.mail.host / username / password).");
        }
    }

    public void envoyerRelanceFacture(Facture facture) {
        ensureMailAvailable();
        if (facture == null || facture.getClient() == null) {
            throw new IllegalArgumentException("La facture ou l'e-mail du client est invalide");
        }

        try {
            Context context = new Context();
            context.setVariable("facture", facture);

            String emailContent = templateEngine.process("emails/relance-facture", context);

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(facture.getClient().getEmail());
            helper.setFrom(expediteurEmail);
            helper.setSubject("Relance - Facture " + facture.getNumero());
            helper.setText(emailContent, true);

            emailSender.send(message);
            logger.info("E-mail de relance envoyé pour la facture {}", facture.getNumero());
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void envoyerEmailGeneral(String destinataire, String sujet, String contenu, boolean isHtml) {
        ensureMailAvailable();
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
