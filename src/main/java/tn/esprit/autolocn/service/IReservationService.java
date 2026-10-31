package tn.esprit.autolocn.service;

import java.util.List;
import tn.esprit.autolocn.domain.Reservation;

/** Service CRUD de la réservation — règle de gestion : date de fin >= date de début. */
public interface IReservationService {

    Reservation create(Reservation reservation);

    Reservation findById(Long id);

    List<Reservation> findAll();

    Reservation update(Long id, Reservation reservation);

    void deleteById(Long id);
}
