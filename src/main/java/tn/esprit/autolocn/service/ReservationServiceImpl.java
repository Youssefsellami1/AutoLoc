package tn.esprit.autolocn.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autolocn.domain.Reservation;
import tn.esprit.autolocn.exception.ResourceNotFoundException;
import tn.esprit.autolocn.repository.IReservationRepository;

/**
 * Service de la réservation : injection par constructeur, service sans état.
 * Les associations (client, véhicule) sont persistées telles que reçues :
 * leur gestion est au Chapitre 9.
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements IReservationService {

    private final IReservationRepository reservationRepository;

    @Override
    public Reservation create(Reservation reservation) {
        if (reservation.getIdReservation() != null) {
            throw new IllegalArgumentException("Une nouvelle réservation ne doit pas avoir d'identifiant");
        }
        valider(reservation);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    @Override
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation update(Long id, Reservation reservation) {
        Reservation existante = findById(id);
        existante.setDateDebut(reservation.getDateDebut());
        existante.setDateFin(reservation.getDateFin());
        existante.setStatut(reservation.getStatut());
        valider(existante);
        return reservationRepository.save(existante);
    }

    @Override
    public void deleteById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation", id);
        }
        reservationRepository.deleteById(id);
    }

    /** Règle de gestion : dates obligatoires et date de fin non antérieure à la date de début. */
    private void valider(Reservation reservation) {
        if (reservation.getDateDebut() == null || reservation.getDateFin() == null) {
            throw new IllegalArgumentException(
                    "Les dates de début et de fin de la réservation sont obligatoires");
        }
        if (reservation.getDateFin().isBefore(reservation.getDateDebut())) {
            throw new IllegalArgumentException(
                    "La date de fin de la réservation ne peut pas être antérieure à la date de début");
        }
    }
}
