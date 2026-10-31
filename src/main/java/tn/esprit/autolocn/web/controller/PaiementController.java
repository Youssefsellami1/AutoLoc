package tn.esprit.autolocn.web.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autolocn.domain.Paiement;
import tn.esprit.autolocn.service.IPaiementService;

/**
 * Contrôleur REST des paiements : lecture seule (le Contrat porte ses paiements).
 * Le POST /api/paiements est volontairement absent → 405 Method Not Allowed.
 */
@RestController
@RequestMapping("/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final IPaiementService paiementService;

    @GetMapping
    public List<Paiement> findAll() {
        return paiementService.findAll();
    }

    @GetMapping("/{id}")
    public Paiement findById(@PathVariable Long id) {
        return paiementService.findById(id);
    }
}
