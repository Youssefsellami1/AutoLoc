package tn.esprit.autolocn.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autolocn.domain.Reservation;

/**
 * Repository de l'entité Reservation.
 */
public interface IReservationRepository extends JpaRepository<Reservation, Long> {

    /** Réservations d'un client (dérivé de l'association {@code Reservation.client}). */
    List<Reservation> findByClient_IdClient(Long idClient);
}
