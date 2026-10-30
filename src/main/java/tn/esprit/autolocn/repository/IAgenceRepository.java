package tn.esprit.autolocn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autolocn.domain.Agence;

/**
 * Repository de l'entité Agence.
 */
public interface IAgenceRepository extends JpaRepository<Agence, Long> {
}
