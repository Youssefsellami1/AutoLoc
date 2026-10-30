package tn.esprit.autolocn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autolocn.domain.Employe;

/**
 * Repository de l'entité Employe.
 */
public interface IEmployeRepository extends JpaRepository<Employe, Long> {
}
