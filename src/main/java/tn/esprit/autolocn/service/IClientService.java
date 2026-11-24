package tn.esprit.autolocn.service;

import java.util.List;
import tn.esprit.autolocn.domain.Client;
import tn.esprit.autolocn.dto.ClientRequestDto;
import tn.esprit.autolocn.dto.ClientResponseDto;

/**
 * Service CRUD du client — règle de gestion : nom et prénom obligatoires
 * (activité DTO : les entrées/sorties passent par les DTO).
 */
public interface IClientService {

    /** Ajoute un client à partir du DTO reçu et renvoie le DTO créé (201). */
    ClientResponseDto create(ClientRequestDto clientRequestDto);

    /** Consulte un client par son identifiant et renvoie le DTO de réponse (sans e-mail). */
    ClientResponseDto findByIdDto(Long id);

    List<Client> findAll();

    Client update(Long id, Client client);

    void deleteById(Long id);
}
