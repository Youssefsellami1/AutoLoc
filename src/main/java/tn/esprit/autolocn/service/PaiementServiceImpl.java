package tn.esprit.autolocn.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autolocn.domain.Paiement;
import tn.esprit.autolocn.exception.ResourceNotFoundException;
import tn.esprit.autolocn.repository.IPaiementRepository;

/**
 * Service du paiement : lecture seule.
 * Délibérément aucune méthode create/update/delete — le code métier crée ou
 * retire un paiement à travers le Contrat (règle de composition Atelier 3).
 */
@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements IPaiementService {

    private final IPaiementRepository paiementRepository;

    @Override
    public Paiement findById(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", id));
    }

    @Override
    public List<Paiement> findAll() {
        return paiementRepository.findAll();
    }
}
