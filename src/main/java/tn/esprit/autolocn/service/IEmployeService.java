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

    /**
     * Affecte l'employé à l'agence (relation {@code @ManyToOne} bidirectionnelle).
     * Un employé n'appartient qu'à une seule agence à la fois : si l'employé est
     * déjà affecté ailleurs, le transfert passe d'abord par une désaffectation.
     */
    Employe affecterEmploye(Long idEmploye, Long idAgence);

    /** Désaffecte l'employé de son agence : l'employé subsiste, simplement sans agence. */
    Employe desaffecterEmploye(Long idEmploye);
}
