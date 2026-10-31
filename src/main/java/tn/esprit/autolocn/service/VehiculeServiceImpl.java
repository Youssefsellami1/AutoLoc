package tn.esprit.autolocn.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autolocn.domain.Agence;
import tn.esprit.autolocn.domain.Equipement;
import tn.esprit.autolocn.domain.Vehicule;
import tn.esprit.autolocn.exception.ResourceNotFoundException;
import tn.esprit.autolocn.repository.IAgenceRepository;
import tn.esprit.autolocn.repository.IEquipementRepository;
import tn.esprit.autolocn.repository.IVehiculeRepository;

/** Service du véhicule : injection par constructeur, service sans état. */
@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements IVehiculeService {

    private final IVehiculeRepository vehiculeRepository;
    private final IAgenceRepository agenceRepository;
    private final IEquipementRepository equipementRepository;

    @Override
    public Vehicule create(Vehicule vehicule) {
        if (vehicule.getIdVehicule() != null) {
            throw new IllegalArgumentException("Un nouveau véhicule ne doit pas avoir d'identifiant");
        }
        valider(vehicule);
        return vehiculeRepository.save(vehicule);
    }

    @Override
    public Vehicule findById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
    }

    @Override
    public List<Vehicule> findAll() {
        return vehiculeRepository.findAll();
    }

    @Override
    public Vehicule update(Long id, Vehicule vehicule) {
        Vehicule existant = findById(id);
        existant.setImmatriculation(vehicule.getImmatriculation());
        existant.setMarque(vehicule.getMarque());
        existant.setModele(vehicule.getModele());
        existant.setCategorie(vehicule.getCategorie());
        existant.setTarifJournalier(vehicule.getTarifJournalier());
        existant.setStatut(vehicule.getStatut());
        valider(existant);
        return vehiculeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicule", id);
        }
        vehiculeRepository.deleteById(id);
    }

    /**
     * Scénario 2 — rattache un véhicule à son agence ({@code @ManyToOne} bidirectionnelle).
     * Seul le côté propriétaire ({@code Vehicule.agence}, porteur de {@code agence_id}) est
     * modifié pour persister le rattachement ; la liste inverse {@code Agence.vehicules} de la
     * nouvelle agence est mise à jour en mémoire.
     * <b>Piège orphanRemoval</b> : ne jamais retirer le véhicule de la liste de son ancienne
     * agence — avec {@code orphanRemoval = true}, Hibernate le SUPPRIMERAIT de la base au flush.
     */
    @Override
    @Transactional
    public Vehicule affecterVehicule(Long idVehicule, Long idAgence) {
        Vehicule vehicule = vehiculeRepository.findById(idVehicule)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", idVehicule));
        Agence agence = agenceRepository.findById(idAgence)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", idAgence));

        Agence agenceActuelle = vehicule.getAgence();
        if (agenceActuelle != null && !agenceActuelle.getIdAgence().equals(idAgence)) {
            // Piège : NE PAS faire agenceActuelle.getVehicules().remove(vehicule)
            // (orphanRemoval = true supprimerait le véhicule de la base !).
            vehicule.setAgence(null);       // côté propriétaire seulement
            vehiculeRepository.save(vehicule);
        }

        vehicule.setAgence(agence);          // côté propriétaire : porteur de agence_id
        agence.getVehicules().add(vehicule); // côté inverse de la NOUVELLE agence (transaction)
        return vehiculeRepository.save(vehicule);
    }

    /**
     * Scénario 3 — installe un équipement sur le véhicule ({@code @ManyToMany} bidirectionnelle).
     * Seul le côté propriétaire ({@code Vehicule.equipements}, porteur du {@code @JoinTable})
     * persiste la table associative {@code vehicule_equipement} ; le côté inverse
     * ({@code Equipement.vehicules}, {@code mappedBy}) n'est qu'une vue en mémoire.
     */
    @Override
    @Transactional
    public Vehicule affecterEquipement(Long idVehicule, Long idEquipement) {
        Vehicule vehicule = vehiculeRepository.findById(idVehicule)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", idVehicule));
        Equipement equipement = equipementRepository.findById(idEquipement)
                .orElseThrow(() -> new ResourceNotFoundException("Equipement", idEquipement));

        vehicule.getEquipements().add(equipement);   // côté propriétaire -> table associative
        equipement.getVehicules().add(vehicule);     // côté inverse (cohérence mémoire)
        return vehiculeRepository.save(vehicule);
    }

    /** Scénario 3 (suite) — retire l'équipement du véhicule. */
    @Override
    @Transactional
    public Vehicule desaffecterEquipement(Long idVehicule, Long idEquipement) {
        Vehicule vehicule = vehiculeRepository.findById(idVehicule)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", idVehicule));
        Equipement equipement = equipementRepository.findById(idEquipement)
                .orElseThrow(() -> new ResourceNotFoundException("Equipement", idEquipement));

        vehicule.getEquipements().remove(equipement); // DELETE de la ligne de la table associative
        equipement.getVehicules().remove(vehicule);   // côté inverse (cohérence mémoire)
        return vehiculeRepository.save(vehicule);
    }

    /** Règle de gestion : le tarif journalier est obligatoire et ne peut pas être négatif. */
    private void valider(Vehicule vehicule) {
        if (vehicule.getTarifJournalier() == null || vehicule.getTarifJournalier().signum() < 0) {
            throw new IllegalArgumentException(
                    "Le tarif journalier du véhicule est obligatoire et ne peut pas être négatif");
        }
    }
}
