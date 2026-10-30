package tn.esprit.autolocn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autolocn.domain.Reservation;

/**
 * Repository de l'entité Reservation.
 */
public interface IReservationRepository extends JpaRepository<Reservation, Long> {
}
