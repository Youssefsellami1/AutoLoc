package tn.esprit.autolocn.exception;

/**
 * Exception métier levée quand une ressource demandée n'existe pas.
 * Le service traduit ainsi une absence (Optional vide) en erreur explicite,
 * au lieu de propager un null ou un Optional.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String ressource, Long id) {
        super(ressource + " introuvable avec l'identifiant " + id);
    }
}
