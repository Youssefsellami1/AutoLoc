package tn.esprit.autolocn.service;

import java.util.List;
import tn.esprit.autolocn.domain.Client;

/** Service CRUD du client — règle de gestion : nom et e-mail obligatoires. */
public interface IClientService {

    Client create(Client client);

    Client findById(Long id);

    List<Client> findAll();

    Client update(Long id, Client client);

    void deleteById(Long id);
}
