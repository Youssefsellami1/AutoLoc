package tn.esprit.autolocn.web.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autolocn.dto.ReservationResponseDto;
import tn.esprit.autolocn.service.IReservationService;

/**
 * Contrôleur REST des réservations — Activité DTO, scénario 2.
 *
 * <p>Seule la <b>consultation</b> est exposée, et uniquement avec des DTO
 * aplatis : le JSON renvoyé ne contient jamais d'objet {@code Client} ou
 * {@code Vehicule} imbriqué (pas d'entité JPA dans la réponse).</p>
 */
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService reservationService;

    /** GET /api/reservations — toutes les réservations (DTO aplatis). */
    @GetMapping
    public List<ReservationResponseDto> findAll() {
        return reservationService.findAll();
    }

    /** GET /api/reservations/client/{idClient} — les réservations d'un client. */
    @GetMapping("/client/{idClient}")
    public List<ReservationResponseDto> findByClientId(@PathVariable Long idClient) {
        return reservationService.findByClientId(idClient);
    }
}
