package lab.hang.Gestion.boulangerie.controller;

import lab.hang.Gestion.boulangerie.model.*;
import lab.hang.Gestion.boulangerie.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/bulletins")
public class BulletinDePaieController {

    private final BulletinDePaieService bulletinService;
    private final EmployeService employeService;
    private final UserService userService;

    public BulletinDePaieController(BulletinDePaieService bulletinService,
                                    EmployeService employeService,
                                    UserService userService) {
        this.bulletinService = bulletinService;
        this.employeService  = employeService;
        this.userService     = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String list(@RequestParam(required = false) Integer mois,
                       @RequestParam(required = false) Integer annee,
                       @RequestParam(required = false) StatutBulletin statut,
                       @RequestParam(required = false) Long employeId,
                       Model model) {
        int m = mois  != null ? mois  : LocalDate.now().getMonthValue();
        int a = annee != null ? annee : LocalDate.now().getYear();
        LocalDate debut = LocalDate.of(a, m, 1);
        LocalDate fin   = debut.withDayOfMonth(debut.lengthOfMonth());

        List<BulletinDePaie> bulletins;
        if (employeId != null) {
            Employe emp = employeService.getById(employeId);
            bulletins = bulletinService.getBulletinsParEmploye(emp).stream()
                .filter(b -> !b.getPeriode().isBefore(debut) && !b.getPeriode().isAfter(fin))
                .filter(b -> statut == null || b.getStatut() == statut)
                .toList();
        } else {
            bulletins = bulletinService.getBulletinsFiltres(debut, fin, statut);
        }

        long nbGenere   = bulletins.stream().filter(b -> b.getStatut() == StatutBulletin.GENERE).count();
        double totalNet = bulletins.stream().filter(b -> b.getStatut() == StatutBulletin.GENERE)
                            .mapToDouble(BulletinDePaie::getSalaireNet).sum();
        double totalPaye = bulletins.stream().filter(b -> b.getStatut() == StatutBulletin.PAYE)
                            .mapToDouble(BulletinDePaie::getSalaireNet).sum();

        model.addAttribute("bulletins", bulletins);
        model.addAttribute("employes", employeService.getAllActifs());
        model.addAttribute("moisActuel", m);
        model.addAttribute("anneeActuelle", a);
        model.addAttribute("statutFiltre", statut);
        model.addAttribute("employeIdFiltre", employeId);
        model.addAttribute("nbGenere", nbGenere);
        model.addAttribute("totalNet", totalNet);
        model.addAttribute("totalPaye", totalPaye);
        return "bulletins/list";
    }

    @GetMapping("/generer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String showGenererForm(@RequestParam(required = false) Long employeId, Model model) {
        model.addAttribute("employes", employeService.getAllActifs());
        model.addAttribute("employeIdPrefill", employeId);
        model.addAttribute("moisActuel", LocalDate.now().getMonthValue());
        model.addAttribute("anneeActuelle", LocalDate.now().getYear());
        return "bulletins/generer";
    }

    @PostMapping("/generer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String generer(@RequestParam Long employeId,
                          @RequestParam int mois,
                          @RequestParam int annee,
                          @RequestParam(defaultValue = "0") double primes,
                          @RequestParam(defaultValue = "0") double indemnitesTransport,
                          @RequestParam(defaultValue = "0") double avanceSurSalaire,
                          RedirectAttributes ra) {
        LocalDate periode = LocalDate.of(annee, mois, 1);
        try {
            BulletinDePaie bulletin = bulletinService.genererBulletin(
                employeId, periode, primes, indemnitesTransport, avanceSurSalaire);
            ra.addFlashAttribute("successMessage",
                "Bulletin généré pour " + bulletin.getEmploye().getNom() + ".");
            return "redirect:/bulletins/" + bulletin.getId();
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bulletins/generer";
        }
    }

    @GetMapping("/mes-bulletins")
    public String mesBulletins(Model model) {
        User currentUser = userService.getCurrentUser();
        employeService.findByUser(currentUser).ifPresentOrElse(
            employe -> {
                model.addAttribute("employe", employe);
                model.addAttribute("bulletins", bulletinService.getBulletinsParEmploye(employe));
            },
            () -> model.addAttribute("aucunProfil", true)
        );
        return "bulletins/mes-bulletins";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or @bulletinSecurity.isBulletinOwner(#id, authentication)")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("bulletin", bulletinService.getById(id));
        return "bulletins/detail";
    }

    @PostMapping("/{id}/payer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String payer(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bulletinService.payerBulletin(id);
            ra.addFlashAttribute("successMessage", "Paiement effectué avec succès.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/bulletins/" + id;
    }
}
