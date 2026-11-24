package tn.esprit.autolocn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de réception (request) pour un client — Activité DTO, scénario 1.
 *
 * <p>Il ne contient QUE les champs que l'API autorise à recevoir :
 * le nom et le prénom sont obligatoires (validation {@code @NotBlank}),
 * l'e-mail et le téléphone sont facultatifs. Les champs internes de
 * l'entité (identifiant, numéro de permis, date d'inscription, réservations)
 * ne sont jamais reçus du client : c'est le contrôle des données reçues.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequestDto {

    @NotBlank(message = "Le nom du client est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom du client est obligatoire")
    private String prenom;

    /** Facultatif — l'entité l'accepte maintenant à NULL (voir Client). */
    private String email;

    /** Facultatif. */
    private String telephone;
}
