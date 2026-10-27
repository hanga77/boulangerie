package lab.hang.Gestion.boulangerie.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Phase 7 — guide de pilote terrain (1 journée type).
 */
@Controller
@RequestMapping("/admin/pilot")
@PreAuthorize("hasRole('ADMIN')")
public class PilotGuideController {

    @GetMapping
    public String guide() {
        return "admin/pilot";
    }
}
