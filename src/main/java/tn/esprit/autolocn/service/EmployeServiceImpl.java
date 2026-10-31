package tn.esprit.autolocn.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autolocn.domain.Agence;
import tn.esprit.autolocn.domain.Employe;
import tn.esprit.autolocn.exception.ResourceNotFoundException;
import tn.esprit.autolocn.repository.IAgenceRepository;
import tn.esprit.autolocn.repository.IEmployeRepository;

/** Service de l'employé : injection par constructeur, service sans état. */
@Service
@RequiredArgsConstructor
public class EmployeServiceImpl implements IEmployeService {

    private final IEmployeRepository employeRepository;
    private final IAgenceRepository agenceRepository;

    @Override
    public Employe create(Employe employe) {
        if (employe.getIdEmploye() != null) {
            throw new IllegalArgumentException("Un nouvel employé ne doit pas avoir d'identifiant");
        }
        valider(employe);
        return employeRepository.save(employe);
    }

    @Override
    public Employe findById(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", id));
    }

    @Override
    public List<Employe> findAll() {
        return employeRepository.findAll();
    }

    @Override
    public Employe update(Long id, Employe employe) {
        Employe existant = findById(id);
        existant.setNom(employe.getNom());
        existant.setPrenom(employe.getPrenom());
        existant.setRole(employe.getRole());
        valider(existant);
        return employeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!employeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employe", id);
        }
        employeRepository.deleteById(id);
    }

    /**
     * Scénario 1 — transfert d'un employé entre agences (relation {@code @ManyToOne} bidirectionnelle).
     *
     * <p>Gestion des deux côtés de la relation :</p>
     * <ul>
     *   <li>côté <b>propriétaire</b> ({@code Employe.agence}, porteur de la clé étrangère
     *       {@code agence_id}) : seul lui est modifié pour persister le transfert ;</li>
     *   <li>côté <b>inverse</b> ({@code Agence.employes}) : la liste de la <b>nouvelle</b> agence
     *       est mise à jour en mémoire pour la cohérence de la session.</li>
     * </ul>
     *
     * <p><b>Piège orphanRemoval (constaté en SQL)</b> : avec {@code orphanRemoval = true} sur
     * {@code Agence.employes}, retirer l'employé de la liste de son ancienne agence — même sans
     * {@code save()} explicite de l'agence — fait le SUPPRIMER de la base au flush : l'agence est
     * une entité gérée dans la transaction, sa collection sale est automatiquement flushée.
     * On ne touche donc <b>jamais</b> à la liste de l'ancienne agence.</p>
     */
    @Override
    @Transactional
    public Employe affecterEmploye(Long idEmploye, Long idAgence) {
        Employe employe = employeRepository.findById(idEmploye)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", idEmploye));
        Agence agence = agenceRepository.findById(idAgence)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", idAgence));

        // Transfert : un employé n'appartient qu'à une seule agence à la fois.
        Agence agenceActuelle = employe.getAgence();
        if (agenceActuelle != null && !agenceActuelle.getIdAgence().equals(idAgence)) {
            // Piège : NE PAS faire agenceActuelle.getEmployes().remove(employe)
            // (orphanRemoval = true supprimerait l'employé de la base !).
            employe.setAgence(null);         // côté propriétaire seulement
            employeRepository.save(employe); // clé étrangère remise à NULL
        }

        employe.setAgence(agence);          // côté propriétaire : porteur de agence_id
        agence.getEmployes().add(employe);  // côté inverse de la NOUVELLE agence (transaction)
        return employeRepository.save(employe);
    }

    /**
     * Désaffectation : l'employé subsiste, sans agence (agence_id = NULL).
     * Même piège orphanRemoval : on ne retire pas l'employé de la liste de l'agence,
     * on ne modifie que le côté propriétaire.
     */
    @Override
    @Transactional
    public Employe desaffecterEmploye(Long idEmploye) {
        Employe employe = employeRepository.findById(idEmploye)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", idEmploye));
        if (employe.getAgence() != null) {
            // Piège : ne PAS faire employe.getAgence().getEmployes().remove(employe)
            employe.setAgence(null); // côté propriétaire : UPDATE agence_id = NULL
        }
        return employeRepository.save(employe);
    }

    /** Règle de gestion : nom et prénom de l'employé sont obligatoires (modèle : pas de « poste »). */
    private void valider(Employe employe) {
        if (employe.getNom() == null || employe.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom de l'employé est obligatoire");
        }
        if (employe.getPrenom() == null || employe.getPrenom().isBlank()) {
            throw new IllegalArgumentException("Le prénom de l'employé est obligatoire");
        }
    }
}
