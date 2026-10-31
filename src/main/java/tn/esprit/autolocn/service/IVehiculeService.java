package tn.esprit.autolocn.service;

import java.util.List;
import tn.esprit.autolocn.domain.Vehicule;

/** Service CRUD du véhicule — règle de gestion : tarif journalier non négatif. */
public interface IVehiculeService {

    Vehicule create(Vehicule vehicule);

    Vehicule findById(Long id);

    List<Vehicule> findAll();

    Vehicule update(Long id, Vehicule vehicule);

    void deleteById(Long id);
}
