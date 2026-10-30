package tn.esprit.autolocn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autolocn.domain.Equipement;

/**
 * Repository de l'entité Equipement.
 */
public interface IEquipementRepository extends JpaRepository<Equipement, Long> {
}
