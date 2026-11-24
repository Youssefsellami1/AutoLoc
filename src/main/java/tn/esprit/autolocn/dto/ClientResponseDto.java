package tn.esprit.autolocn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de réponse (response) pour un client — Activité DTO, scénario 1.
 *
 * <p>Il ne contient que les champs EXPOSÉS au client de l'API :
 * identifiant, nom, prénom et téléphone. L'e-mail est volontairement
 * absent : c'est la personnalisation des données renvoyées (pas de fuite
 * de donnée sensible, contrat JSON stable même si le schéma JPA change).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDto {

    private Long idClient;
    private String nom;
    private String prenom;
    private String telephone;
}
