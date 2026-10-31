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

    /** Rattache le véhicule à une agence (relation {@code @ManyToOne} bidirectionnelle, scénario 2). */
    Vehicule affecterVehicule(Long idVehicule, Long idAgence);

    /** Ajoute un équipement au véhicule (relation {@code @ManyToMany} bidirectionnelle, scénario 3). */
    Vehicule affecterEquipement(Long idVehicule, Long idEquipement);

    /** Retire un équipement du véhicule (scénario 3 — retrait au retour du véhicule). */
    Vehicule desaffecterEquipement(Long idVehicule, Long idEquipement);
}
