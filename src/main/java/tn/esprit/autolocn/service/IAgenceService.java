package tn.esprit.autolocn.service;

import java.util.List;
import tn.esprit.autolocn.domain.Agence;

/** Service CRUD de l'agence — règle de gestion : le nom est obligatoire. */
public interface IAgenceService {

    Agence create(Agence agence);

    Agence findById(Long id);

    List<Agence> findAll();

    Agence update(Long id, Agence agence);

    void deleteById(Long id);
}
