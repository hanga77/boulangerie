package lab.hang.Gestion.boulangerie.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lab.hang.Gestion.boulangerie.service.LicenseService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LicenseInterceptor implements HandlerInterceptor {

    private final LicenseService licenseService;

    public LicenseInterceptor(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // Toujours autoriser : lecture, login, logout, assets, page licence
        if ("GET".equalsIgnoreCase(method)
                || uri.startsWith("/login")
                || uri.startsWith("/logout")
                || uri.startsWith("/css")
                || uri.startsWith("/js")
                || uri.startsWith("/uploads")
                || uri.startsWith("/admin/licence")) {
            return true;
        }

        LicenseService.Status status = licenseService.getStatus();
        if (status == LicenseService.Status.DEMO_EXPIRED) {
            response.sendRedirect("/admin/licence?expired=true");
            return false;
        }
        return true;
    }
}
