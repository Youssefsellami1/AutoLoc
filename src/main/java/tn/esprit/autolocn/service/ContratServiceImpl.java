package tn.esprit.autolocn.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autolocn.domain.Contrat;
import tn.esprit.autolocn.exception.ResourceNotFoundException;
import tn.esprit.autolocn.repository.IContratRepository;

/**
 * Implémentation du service Contrat.
 *
 * <p>Choix de conception (justifiés dans docs/service-notes.md) :</p>
 * <ul>
 *   <li>{@code @Service} : bean détecté par le scan du package racine, placé dans la couche métier.</li>
 *   <li>Champ {@code final} + {@code @RequiredArgsConstructor} : Lombok génère le constructeur ;
 *       Spring, constatant un constructeur unique, l'utilise pour l'injection SANS {@code @Autowired}.</li>
 *   <li>Type {@code IContratRepository} (interface) : le service dépend d'une abstraction.</li>
 *   <li>Service sans état : aucun attribut modifiable (singleton partagé par tous les threads).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ContratServiceImpl implements IContratService {

    private final IContratRepository contratRepository;

    @Override
    public Contrat create(Contrat contrat) {
        if (contrat.getIdContrat() != null) {
            throw new IllegalArgumentException(
                    "Un nouveau contrat ne doit pas avoir d'identifiant");
        }
        valider(contrat);
        return contratRepository.save(contrat);
    }

    @Override
    public Contrat findById(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", id));
    }

    @Override
    public List<Contrat> findAll() {
        return contratRepository.findAll();
    }

    @Override
    public Contrat update(Long id, Contrat contrat) {
        // Charger l'existant puis recopier : passer l'objet reçu directement à save()
        // écraserait toutes les colonnes et, avec orphanRemoval, pourrait vider les paiements.
        Contrat existant = findById(id);
        existant.setDateSignature(contrat.getDateSignature());
        existant.setMontantTotal(contrat.getMontantTotal());
        existant.setValide(contrat.getValide());
        valider(existant);
        return contratRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        // deleteById du repository ne lève aucune exception si l'id n'existe pas :
        // c'est au service d'imposer la règle. La cascade supprime les paiements.
        if (!contratRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contrat", id);
        }
        contratRepository.deleteById(id);
    }

    /** Règle de gestion : le montant total est obligatoire et ne peut pas être négatif. */
    private void valider(Contrat contrat) {
        if (contrat.getMontantTotal() == null || contrat.getMontantTotal().signum() < 0) {
            throw new IllegalArgumentException(
                    "Le montant total du contrat est obligatoire et ne peut pas être négatif");
        }
    }
}
