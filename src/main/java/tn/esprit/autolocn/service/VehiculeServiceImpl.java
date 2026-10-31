package tn.esprit.autolocn.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autolocn.domain.Vehicule;
import tn.esprit.autolocn.exception.ResourceNotFoundException;
import tn.esprit.autolocn.repository.IVehiculeRepository;

/** Service du véhicule : injection par constructeur, service sans état. */
@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements IVehiculeService {

    private final IVehiculeRepository vehiculeRepository;

    @Override
    public Vehicule create(Vehicule vehicule) {
        if (vehicule.getIdVehicule() != null) {
            throw new IllegalArgumentException("Un nouveau véhicule ne doit pas avoir d'identifiant");
        }
        valider(vehicule);
        return vehiculeRepository.save(vehicule);
    }

    @Override
    public Vehicule findById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
    }

    @Override
    public List<Vehicule> findAll() {
        return vehiculeRepository.findAll();
    }

    @Override
    public Vehicule update(Long id, Vehicule vehicule) {
        Vehicule existant = findById(id);
        existant.setImmatriculation(vehicule.getImmatriculation());
        existant.setMarque(vehicule.getMarque());
        existant.setModele(vehicule.getModele());
        existant.setCategorie(vehicule.getCategorie());
        existant.setTarifJournalier(vehicule.getTarifJournalier());
        existant.setStatut(vehicule.getStatut());
        valider(existant);
        return vehiculeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicule", id);
        }
        vehiculeRepository.deleteById(id);
    }

    /** Règle de gestion : le tarif journalier est obligatoire et ne peut pas être négatif. */
    private void valider(Vehicule vehicule) {
        if (vehicule.getTarifJournalier() == null || vehicule.getTarifJournalier().signum() < 0) {
            throw new IllegalArgumentException(
                    "Le tarif journalier du véhicule est obligatoire et ne peut pas être négatif");
        }
    }
}
