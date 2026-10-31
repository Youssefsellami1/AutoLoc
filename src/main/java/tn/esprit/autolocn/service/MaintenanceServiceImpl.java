package tn.esprit.autolocn.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autolocn.domain.Maintenance;
import tn.esprit.autolocn.exception.ResourceNotFoundException;
import tn.esprit.autolocn.repository.IMaintenanceRepository;

/** Service de la maintenance : injection par constructeur, service sans état. */
@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements IMaintenanceService {

    private final IMaintenanceRepository maintenanceRepository;

    @Override
    public Maintenance create(Maintenance maintenance) {
        if (maintenance.getIdMaintenance() != null) {
            throw new IllegalArgumentException("Une nouvelle maintenance ne doit pas avoir d'identifiant");
        }
        valider(maintenance);
        return maintenanceRepository.save(maintenance);
    }

    @Override
    public Maintenance findById(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance", id));
    }

    @Override
    public List<Maintenance> findAll() {
        return maintenanceRepository.findAll();
    }

    @Override
    public Maintenance update(Long id, Maintenance maintenance) {
        Maintenance existante = findById(id);
        existante.setDateDebut(maintenance.getDateDebut());
        existante.setDateFin(maintenance.getDateFin());
        existante.setDescription(maintenance.getDescription());
        valider(existante);
        return maintenanceRepository.save(existante);
    }

    @Override
    public void deleteById(Long id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Maintenance", id);
        }
        maintenanceRepository.deleteById(id);
    }

    /** Règle de gestion : dates obligatoires et date de fin non antérieure à la date de début
     *  (pas d'attribut « coût » dans ce modèle). */
    private void valider(Maintenance maintenance) {
        if (maintenance.getDateDebut() == null) {
            throw new IllegalArgumentException("La date de début de la maintenance est obligatoire");
        }
        if (maintenance.getDateFin() != null && maintenance.getDateFin().isBefore(maintenance.getDateDebut())) {
            throw new IllegalArgumentException(
                    "La date de fin de la maintenance ne peut pas être antérieure à la date de début");
        }
    }
}
