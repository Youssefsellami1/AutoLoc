package tn.esprit.autolocn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autolocn.domain.Paiement;

/**
 * Repository de l'entité Paiement (enfant de Contrat, composition).
 * Créé uniquement pour LIRE les paiements : le code métier doit créer ou
 * retirer un paiement à travers le Contrat (cascade REMOVE, orphanRemoval).
 */
public interface IPaiementRepository extends JpaRepository<Paiement, Long> {
}
