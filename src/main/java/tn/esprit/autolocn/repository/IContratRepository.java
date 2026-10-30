package tn.esprit.autolocn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autolocn.domain.Contrat;

/**
 * Repository de l'entité Contrat (fil rouge de l'Atelier 3).
 * JpaRepository réunit CrudRepository (CRUD complet, findAll renvoie une List),
 * PagingAndSortingRepository (tri et pagination), QueryByExampleExecutor et les
 * méthodes propres à JPA (flush, suppressions en lot).
 */
public interface IContratRepository extends JpaRepository<Contrat, Long> {
}
