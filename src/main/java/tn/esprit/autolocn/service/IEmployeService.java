package tn.esprit.autolocn.service;

import java.util.List;
import tn.esprit.autolocn.domain.Employe;

/** Service CRUD de l'employé — règle de gestion : nom et prénom obligatoires. */
public interface IEmployeService {

    Employe create(Employe employe);

    Employe findById(Long id);

    List<Employe> findAll();

    Employe update(Long id, Employe employe);

    void deleteById(Long id);
}
