package tn.esprit.autolocn.service;

import java.util.List;
import tn.esprit.autolocn.domain.Reservation;
import tn.esprit.autolocn.dto.ReservationResponseDto;

/**
 * Service CRUD de la réservation — règle de gestion : date de fin >= date de début.
 * Activité DTO : la consultation retourne des DTO aplatis (sans objet
 * Client/Vehicule imbriqué).
 */
public interface IReservationService {

    Reservation create(Reservation reservation);

    Reservation findById(Long id);

    /** Toutes les réservations, en DTO aplatis (client et véhicule à plat). */
    List<ReservationResponseDto> findAll();

    /** Les réservations d'un client donné, en DTO aplatis. */
    List<ReservationResponseDto> findByClientId(Long idClient);

    Reservation update(Long id, Reservation reservation);

    void deleteById(Long id);
}
