package tn.esprit.autolocn.demo;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tn.esprit.autolocn.domain.Agence;
import tn.esprit.autolocn.domain.CategorieVehicule;
import tn.esprit.autolocn.domain.Employe;
import tn.esprit.autolocn.domain.Equipement;
import tn.esprit.autolocn.domain.RoleEmploye;
import tn.esprit.autolocn.domain.StatutVehicule;
import tn.esprit.autolocn.domain.Vehicule;
import tn.esprit.autolocn.repository.IAgenceRepository;
import tn.esprit.autolocn.repository.IEquipementRepository;
import tn.esprit.autolocn.repository.IEmployeRepository;
import tn.esprit.autolocn.repository.IVehiculeRepository;
import tn.esprit.autolocn.service.IEmployeService;
import tn.esprit.autolocn.service.IVehiculeService;

/**
 * Démonstration de l'Atelier 6 : relations BIDIRECTIONNELLES gérées dans la couche Service.
 *
 * <ul>
 *   <li>Scénario 1 — {@code @ManyToOne} Employe → Agence : affectation, transfert
 *       (désaffecter puis affecter) et désaffectation complète ;</li>
 *   <li>Scénario 2 — {@code @ManyToOne} Vehicule → Agence : véhicule neuf rattaché à Sfax ;</li>
 *   <li>Scénario 3 — {@code @ManyToMany} Vehicule ↔ Equipement : installation du siège bébé
 *       puis retrait du GPS (table associative {@code vehicule_equipement}).</li>
 * </ul>
 *
 * <p>Activation (désactivée par défaut pour ne pas rejouer les scénarios à chaque démarrage) :
 * {@code --autoloc.atelier6.demo=true}. Les données créées restent en base (MySQL/XAMPP) —
 * vérifiables dans phpMyAdmin.</p>
 */
@Component
@ConditionalOnProperty(name = "autoloc.atelier6.demo", havingValue = "true")
@RequiredArgsConstructor
public class Atelier6Demo implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Atelier6Demo.class);

    private final IEmployeService employeService;
    private final IVehiculeService vehiculeService;
    private final IAgenceRepository agenceRepository;
    private final IEmployeRepository employeRepository;
    private final IVehiculeRepository vehiculeRepository;
    private final IEquipementRepository equipementRepository;
    private final PlatformTransactionManager transactionManager;

    @Override
    public void run(String... args) {
        log.info("========== ATELIER 6 : RELATIONS BIDIRECTIONNELLES (couche Service) ==========");

        // ---------- Jeux de données (idempotents : réutilisés s'ils existent déjà) ----------
        Agence agenceTunis = agenceParVille("Tunis", "Agence Tunis Centre");
        Agence agenceSousse = agenceParVille("Sousse", "Agence Sousse Médina");
        Agence agenceSfax = agenceParVille("Sfax", "Agence Sfax Corniche");

        Employe agent = employeParNom("Ben Salah", "Karim");
        Employe agent2 = employeParNom("Trabelsi", "Nadia");
        Vehicule vehicule = vehiculeParImmatriculation("7890-TN-456");
        Equipement siegeBebe = equipementParLibelle("Siège bébé");
        Equipement gps = equipementParLibelle("GPS");

        // ---------- Scénario 1 : @ManyToOne Employe -> Agence ----------
        Employe affecte = employeService.affecterEmploye(agent.getIdEmploye(), agenceTunis.getIdAgence());
        log.info("[S1] Affectation : employé {} ({}) -> agence {} ({})",
                affecte.getIdEmploye(), agent.getNom(), agenceTunis.getNom(), agenceTunis.getVille());

        // Transfert : un employé n'appartient qu'à une agence -> désaffecter puis affecter
        Employe transfere = employeService.affecterEmploye(agent.getIdEmploye(), agenceSousse.getIdAgence());
        log.info("[S1] Transfert (désaffecter + affecter) : employé {} -> agence {} ({})",
                transfere.getIdEmploye(), agenceSousse.getNom(), agenceSousse.getVille());

        // Désaffectation complète : l'employé subsiste, agence_id = NULL
        employeService.affecterEmploye(agent2.getIdEmploye(), agenceTunis.getIdAgence());
        Employe sansAgence = employeService.desaffecterEmploye(agent2.getIdEmploye());
        log.info("[S1] Désaffectation : employé {} -> agence = NULL (la ligne reste dans la table employe)",
                sansAgence.getIdEmploye());

        // ---------- Scénario 2 : @ManyToOne Vehicule -> Agence ----------
        Vehicule rattache = vehiculeService.affecterVehicule(vehicule.getIdVehicule(), agenceSfax.getIdAgence());
        log.info("[S2] Véhicule neuf {} ({}) -> agence {} ({})",
                rattache.getIdVehicule(), vehicule.getImmatriculation(), agenceSfax.getNom(), agenceSfax.getVille());

        // ---------- Scénario 3 : @ManyToMany Vehicule <-> Equipement ----------
        vehiculeService.affecterEquipement(vehicule.getIdVehicule(), siegeBebe.getIdEquipement());
        log.info("[S3] Installation : siège bébé (id {}) monté sur le véhicule {} (table vehicule_equipement)",
                siegeBebe.getIdEquipement(), vehicule.getIdVehicule());

        vehiculeService.affecterEquipement(vehicule.getIdVehicule(), gps.getIdEquipement());
        vehiculeService.desaffecterEquipement(vehicule.getIdVehicule(), gps.getIdEquipement());
        log.info("[S3] Retrait : GPS (id {}) démonté du véhicule {} (ligne supprimée de vehicule_equipement)",
                gps.getIdEquipement(), vehicule.getIdVehicule());

        // ---------- Vérification finale (associations LAZY lues dans une session) ----------
        Long idAgent = agent.getIdEmploye();
        Long idAgent2 = agent2.getIdEmploye();
        Long idVehicule = vehicule.getIdVehicule();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Employe e1 = employeRepository.findById(idAgent).orElseThrow();
            log.info("[Vérif] Employé {} ({}) rattaché à l'agence : {}",
                    e1.getIdEmploye(), e1.getNom(),
                    e1.getAgence() == null ? "NULL" : e1.getAgence().getNom() + " (" + e1.getAgence().getVille() + ")");
            Employe e2 = employeRepository.findById(idAgent2).orElseThrow();
            log.info("[Vérif] Employé {} ({}) rattaché à l'agence : {}",
                    e2.getIdEmploye(), e2.getNom(), e2.getAgence() == null ? "NULL" : e2.getAgence().getNom());
            Vehicule v = vehiculeRepository.findById(idVehicule).orElseThrow();
            log.info("[Vérif] Véhicule {} ({}) : agence = {}, équipements = {}",
                    v.getIdVehicule(), v.getImmatriculation(),
                    v.getAgence() == null ? "NULL" : v.getAgence().getNom() + " (" + v.getAgence().getVille() + ")",
                    v.getEquipements().stream().map(Equipement::getLibelle).toList());
        });

        log.info("========== FIN DE LA DÉMONSTRATION ATELIER 6 ==========");
    }

    // ---------- Création idempotente des données de démonstration ----------

    private Agence agenceParVille(String ville, String nom) {
        return agenceRepository.findAll().stream()
                .filter(a -> ville.equals(a.getVille()))
                .findFirst()
                .orElseGet(() -> {
                    Agence a = new Agence();
                    a.setNom(nom);
                    a.setVille(ville);
                    a.setAdresse("12 Avenue Habib Bourguiba, " + ville);
                    a.setTelephone("71 000 000");
                    return agenceRepository.save(a);
                });
    }

    private Employe employeParNom(String nom, String prenom) {
        return employeRepository.findAll().stream()
                .filter(e -> nom.equals(e.getNom()) && prenom.equals(e.getPrenom()))
                .findFirst()
                .orElseGet(() -> {
                    Employe e = new Employe();
                    e.setNom(nom);
                    e.setPrenom(prenom);
                    e.setRole(RoleEmploye.AGENT);
                    return employeService.create(e);
                });
    }

    private Vehicule vehiculeParImmatriculation(String immatriculation) {
        return vehiculeRepository.findAll().stream()
                .filter(v -> immatriculation.equals(v.getImmatriculation()))
                .findFirst()
                .orElseGet(() -> {
                    Vehicule v = new Vehicule();
                    v.setImmatriculation(immatriculation);
                    v.setMarque("Peugeot");
                    v.setModele("208");
                    v.setCategorie(CategorieVehicule.CITADINE);
                    v.setTarifJournalier(new BigDecimal("120.00"));
                    v.setStatut(StatutVehicule.DISPONIBLE);
                    return vehiculeService.create(v);
                });
    }

    private Equipement equipementParLibelle(String libelle) {
        return equipementRepository.findAll().stream()
                .filter(e -> libelle.equals(e.getLibelle()))
                .findFirst()
                .orElseGet(() -> {
                    Equipement e = new Equipement();
                    e.setLibelle(libelle);
                    return equipementRepository.save(e);
                });
    }
}
