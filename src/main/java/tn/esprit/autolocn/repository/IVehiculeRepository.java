package tn.esprit.autolocn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autolocn.domain.Vehicule;

/**
 * Repository de l'entité Vehicule.
 */
public interface IVehiculeRepository extends JpaRepository<Vehicule, Long> {
}
