package lab.hang.Gestion.boulangerie.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public CustomAuthenticationFailureHandler() {
        super("/login?error");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "Nom d'utilisateur ou mot de passe incorrect";

        if (exception instanceof DisabledException) {
            errorMessage = "Votre compte n'est pas encore activé. Veuillez contacter l'administrateur.";
        }

        FlashMap flashMap = new FlashMap();
        flashMap.put("errorMessage", errorMessage);
        flashMap.setTargetRequestPath("/login");
        new SessionFlashMapManager().saveOutputFlashMap(flashMap, request, response);

        super.onAuthenticationFailure(request, response, exception);
    }
}