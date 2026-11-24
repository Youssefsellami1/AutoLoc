package tn.esprit.autolocn.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.autolocn.domain.StatutReservation;

/**
 * DTO de réponse pour une réservation — Activité DTO, scénario 2.
 *
 * <p>Réservation <b>aplatie</b> : au lieu d'exposer les objets JPA imbriqués
 * {@code Client} et {@code Vehicule} (qui déclencheraient du lazy-loading et
 * un JSON imbriqué instable), le DTO ne contient que des valeurs simples :
 * identifiant, dates, statut, nom/prénom du client et marque/modèle/
 * immatriculation du véhicule.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDto {

    private Long idReservation;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatutReservation statut;

    // Informations du client (à plat, pas d'objet Client)
    private String nomClient;
    private String prenomClient;

    // Informations du véhicule (à plat, pas d'objet Vehicule)
    private String marque;
    private String modele;
    private String immatriculation;
}
