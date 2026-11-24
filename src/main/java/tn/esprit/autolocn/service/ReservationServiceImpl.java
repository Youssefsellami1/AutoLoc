package tn.esprit.autolocn.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autolocn.domain.Reservation;
import tn.esprit.autolocn.dto.ReservationResponseDto;
import tn.esprit.autolocn.exception.ResourceNotFoundException;
import tn.esprit.autolocn.mapper.ReservationMapper;
import tn.esprit.autolocn.repository.IReservationRepository;

/**
 * Service de la réservation : injection par constructeur, service sans état.
 * Les associations (client, véhicule) sont persistées telles que reçues ;
 * la consultation passe par {@link tn.esprit.autolocn.mapper.ReservationMapper}
 * pour produire des DTO aplatis (activité DTO).
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements IReservationService {

    private final IReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

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

    /**
     * Toutes les réservations en DTO. {@code @Transactional} : les associations
     * Client et Vehicule sont LAZY — elles doivent être lues dans la transaction
     * (sinon LazyInitializationException au mapping).
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> findAll() {
        return reservationMapper.toResponseDtoList(reservationRepository.findAll());
    }

    /** Réservations d'un client (association LAZY lue en transaction), en DTO aplatis. */
    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> findByClientId(Long idClient) {
        return reservationMapper.toResponseDtoList(reservationRepository.findByClient_IdClient(idClient));
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
