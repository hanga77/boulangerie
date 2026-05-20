package lab.hang.Gestion.boulangerie.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.io.File;

@ControllerAdvice
public class GlobalModelAttributes {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @ModelAttribute("customLogoUrl")
    public String customLogoUrl() {
        File logo = new File(uploadDir, "logo.png").getAbsoluteFile();
        return logo.exists() ? "/uploads/logo.png" : null;
    }
}
