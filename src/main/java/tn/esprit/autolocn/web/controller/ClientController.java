package tn.esprit.autolocn.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autolocn.dto.ClientRequestDto;
import tn.esprit.autolocn.dto.ClientResponseDto;
import tn.esprit.autolocn.service.IClientService;

/**
 * Contrôleur REST des clients — Activité DTO, scénario 1.
 *
 * <p>Le contrôleur ne manipule <b>que des DTO</b> : l'entité JPA {@code Client}
 * ne quitte plus jamais la couche service. Cela permet de contrôler les données
 * reçues ({@code @Valid} sur le DTO) et de personnaliser les données renvoyées
 * (le {@code ClientResponseDto} n'expose pas l'e-mail).</p>
 *
 * <p>Le {@code /api} du context-path vient de l'application ;
 * les routes sont relatives ("/clients").</p>
 */
@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final IClientService clientService;

    /** POST /api/clients — ajoute un client (nom et prénom obligatoires). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponseDto create(@Valid @RequestBody ClientRequestDto clientRequestDto) {
        return clientService.create(clientRequestDto);
    }

    /** GET /api/clients/{id} — consulte un client (sans e-mail dans la réponse). */
    @GetMapping("/{id}")
    public ClientResponseDto findById(@PathVariable Long id) {
        return clientService.findByIdDto(id);
    }
}
