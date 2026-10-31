package tn.esprit.autolocn.service;

import java.util.List;
import tn.esprit.autolocn.domain.Paiement;

/**
 * Service du paiement — LECTURE SEULE : uniquement findById et findAll.
 * Aucune création, modification ni suppression : elles passent par le Contrat
 * (composition, cascade et orphanRemoval gérés côté Contrat).
 */
public interface IPaiementService {

    Paiement findById(Long id);

    List<Paiement> findAll();
}
