package tn.esprit.autolocn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autolocn.domain.Maintenance;

/**
 * Repository de l'entité Maintenance.
 */
public interface IMaintenanceRepository extends JpaRepository<Maintenance, Long> {
}
