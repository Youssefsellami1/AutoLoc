package tn.esprit.autolocn.service;

import java.util.List;
import tn.esprit.autolocn.domain.Maintenance;

/** Service CRUD de la maintenance — règle de gestion : date de fin >= date de début. */
public interface IMaintenanceService {

    Maintenance create(Maintenance maintenance);

    Maintenance findById(Long id);

    List<Maintenance> findAll();

    Maintenance update(Long id, Maintenance maintenance);

    void deleteById(Long id);
}
