package tn.esprit.autolocn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autolocn.domain.Client;

/**
 * Repository de l'entité Client.
 */
public interface IClientRepository extends JpaRepository<Client, Long> {
}
