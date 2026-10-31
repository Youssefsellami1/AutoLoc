package tn.esprit.autolocn.service;

import java.util.List;
import tn.esprit.autolocn.domain.Equipement;

/** Service CRUD de l'équipement — règle de gestion : libellé obligatoire. */
public interface IEquipementService {

    Equipement create(Equipement equipement);

    Equipement findById(Long id);

    List<Equipement> findAll();

    Equipement update(Long id, Equipement equipement);

    void deleteById(Long id);
}
