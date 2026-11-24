package tn.esprit.autolocn.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autolocn.domain.Client;
import tn.esprit.autolocn.dto.ClientRequestDto;
import tn.esprit.autolocn.dto.ClientResponseDto;
import tn.esprit.autolocn.exception.ResourceNotFoundException;
import tn.esprit.autolocn.mapper.ClientMapper;
import tn.esprit.autolocn.repository.IClientRepository;

/**
 * Service du client : injection par constructeur, service sans état.
 * Activité DTO : l'ajout et la consultation passent par les DTO
 * ({@link ClientMapper} généré par MapStruct fait la conversion).
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements IClientService {

    /** Fuseau horaire explicite — règle SonarQube : ne jamais appeler .now() sans ZoneId ni Clock. */
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final IClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    public ClientResponseDto create(ClientRequestDto clientRequestDto) {
        Client client = clientMapper.toEntity(clientRequestDto); // DTO -> entité (MapStruct)
        valider(client);
        // Champs non reçus via le DTO mais obligatoires en base :
        client.setDateInscription(LocalDate.now(ZONE));
        Client sauvegarde = clientRepository.save(client);
        return clientMapper.toResponseDto(sauvegarde); // entité -> DTO de réponse (sans e-mail)
    }

    @Override
    public ClientResponseDto findByIdDto(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
        return clientMapper.toResponseDto(client); // l'e-mail n'est jamais exposé
    }

    @Override
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    @Override
    public Client update(Long id, Client client) {
        Client existant = findById(id);
        existant.setNom(client.getNom());
        existant.setPrenom(client.getPrenom());
        existant.setEmail(client.getEmail());
        existant.setTelephone(client.getTelephone());
        existant.setNumPermis(client.getNumPermis());
        existant.setDateInscription(client.getDateInscription());
        valider(existant);
        return clientRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client", id);
        }
        clientRepository.deleteById(id);
    }

    private Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    /** Règle de gestion (activité DTO) : nom et prénom du client sont obligatoires. */
    private void valider(Client client) {
        if (client.getNom() == null || client.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom du client est obligatoire");
        }
        if (client.getPrenom() == null || client.getPrenom().isBlank()) {
            throw new IllegalArgumentException("Le prénom du client est obligatoire");
        }
    }
}
