package tn.esprit.autolocn.service;

import java.util.List;
import tn.esprit.autolocn.domain.Contrat;

/**
 * Contrat de service du Contrat (fil rouge de l'Atelier 4).
 * Le service porte la logique métier : il vérifie l'existence, refuse les
 * états incohérents et traduit une absence en exception métier.
 * Il ne connaît ni HTTP ni JSON (ceux-ci viennent à l'Atelier 5).
 */
public interface IContratService {

    /** Crée un contrat — refuse un identifiant déjà renseigné. */
    Contrat create(Contrat contrat);

    /** Charge un contrat — {@code ResourceNotFoundException} si absent. */
    Contrat findById(Long id);

    /** Liste tous les contrats. */
    List<Contrat> findAll();

    /** Met à jour : charge l'existant, recopie les attributs, puis sauvegarde. */
    Contrat update(Long id, Contrat contrat);

    /** Supprime — {@code ResourceNotFoundException} si absent. */
    void deleteById(Long id);
}
