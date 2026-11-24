package tn.esprit.autolocn.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.esprit.autolocn.domain.Reservation;
import tn.esprit.autolocn.dto.ReservationResponseDto;

/**
 * Mapper MapStruct des réservations — Activité DTO, scénario 2.
 *
 * <p>Les champs imbriqués des associations sont « aplatis » dans le DTO
 * grâce aux {@code @Mapping(source = "client.nom", target = "nomClient")} :
 * le JSON renvoyé ne contient <b>jamais</b> d'objet {@code Client} ou
 * {@code Vehicule} imbriqué.</p>
 */
@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "client.nom", target = "nomClient")
    @Mapping(source = "client.prenom", target = "prenomClient")
    @Mapping(source = "vehicule.marque", target = "marque")
    @Mapping(source = "vehicule.modele", target = "modele")
    @Mapping(source = "vehicule.immatriculation", target = "immatriculation")
    ReservationResponseDto toResponseDto(Reservation reservation);

    List<ReservationResponseDto> toResponseDtoList(List<Reservation> reservations);
}
