package lab.hang.Gestion.boulangerie.controller;



import lab.hang.Gestion.boulangerie.model.Fournisseur;
import lab.hang.Gestion.boulangerie.service.FournisseurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fournisseurs")
public class FournisseurController {
    private final FournisseurService fournisseurService;

    public FournisseurController(FournisseurService fournisseurService) {
        this.fournisseurService = fournisseurService;
    }

    @GetMapping
    public List<Fournisseur> getAllFournisseurs() {
        return fournisseurService.getAllFournisseurs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fournisseur> getFournisseur(@PathVariable Long id) {
        return ResponseEntity.ok(fournisseurService.getFournisseurById(id));
    }

    @PostMapping
    public ResponseEntity<Fournisseur> createFournisseur(@RequestBody Fournisseur fournisseur) {
        return ResponseEntity.ok(fournisseurService.saveFournisseur(fournisseur));
    }
}