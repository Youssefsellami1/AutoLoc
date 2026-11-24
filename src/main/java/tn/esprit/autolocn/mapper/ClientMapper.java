package tn.esprit.autolocn.mapper;

import org.mapstruct.Mapper;
import tn.esprit.autolocn.domain.Client;
import tn.esprit.autolocn.dto.ClientRequestDto;
import tn.esprit.autolocn.dto.ClientResponseDto;

/**
 * Mapper MapStruct du client — Activité DTO, scénario 1.
 *
 * <p>{@code componentModel = "spring"} : MapStruct génère
 * {@code ClientMapperImpl} et Spring l'expose comme bean, injectable
 * par constructeur dans le service. Les conversions entité <-> DTO sont
 * donc du code <b>généré</b> (jamais de {@code get/set} manuel).</p>
 *
 * <ul>
 *   <li>{@link #toEntity(ClientRequestDto)} : DTO reçu -> entité JPA à
 *       persister (l'identifiant, le permis et la date restent à null —
 *       le service les gère) ;</li>
 *   <li>{@link #toResponseDto(Client)} : entité JPA -> DTO renvoyé
 *       (l'e-mail n'est pas copié : absent du DTO de réponse).</li>
 * </ul>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ClientMapper {

    /**
     * Request DTO -> entité JPA (contrôle des données reçues).
     * Les champs idClient/numPermis/dateInscription/reservations ne viennent
     * jamais du DTO : ils sont ignorés (permis/identifiant gérés par la base,
     * date d'inscription posée par le service).
     */
    Client toEntity(ClientRequestDto clientRequestDto);

    /** Entité JPA -> Response DTO (personnalisation des données renvoyées). */
    ClientResponseDto toResponseDto(Client client);
}
