package tn.esprit.autolocn.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tn.esprit.autolocn.domain.Contrat;
import tn.esprit.autolocn.domain.ModePaiement;
import tn.esprit.autolocn.domain.Paiement;
import tn.esprit.autolocn.repository.IContratRepository;
import tn.esprit.autolocn.repository.IPaiementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Programme de démonstration de l'Atelier 3 : observe le comportement exact
 * des opérations CRUD (save, findById, deleteById) sur le fil rouge
 * Contrat ↔ Paiement, ainsi que la cascade et l'orphanRemoval.
 *
 * <p>Activation (désactivée par défaut pour ne pas modifier la base à chaque
 * démarrage) : lancer l'application avec
 * {@code --autoloc.atelier3.demo=true} et observer les logs SQL
 * ({@code spring.jpa.show-sql=true} / {@code logging.level.org.hibernate.SQL=DEBUG}).</p>
 *
 * <p>Point pédagogique : chaque appel à un repository s'exécute dans sa propre
 * transaction ; une entité retournée est donc « détachée ». Accéder à une
 * association LAZY (ici {@code Contrat.paiements}) hors transaction lève une
 * {@code LazyInitializationException} — d'où l'usage d'un TransactionTemplate
 * à l'étape 6 pour modifier la collection à travers son parent.</p>
 */
@Component
@ConditionalOnProperty(name = "autoloc.atelier3.demo", havingValue = "true")
public class Atelier3Demo implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Atelier3Demo.class);

    /** Fuseau horaire explicite : sans lui, LocalDate.now() dépend de la machine (règle SonarQube). */
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final IContratRepository contratRepository;
    private final IPaiementRepository paiementRepository;
    private final TransactionTemplate transactionTemplate;

    public Atelier3Demo(IContratRepository contratRepository,
                        IPaiementRepository paiementRepository,
                        PlatformTransactionManager transactionManager) {
        this.contratRepository = contratRepository;
        this.paiementRepository = paiementRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(String... args) {
        log.info("========== ATELIER 3 : DÉMONSTRATION CRUD (Contrat ↔ Paiement) ==========");

        // ----- 1. save() avec id null : persist -> INSERT, id attribué par MySQL -----
        Contrat contrat = new Contrat();
        contrat.setDateSignature(LocalDate.now(ZONE));
        contrat.setMontantTotal(new BigDecimal("1500.00"));
        contrat.setValide(true);

        Contrat contratEnregistre = contratRepository.save(contrat);
        Long contratId = contratEnregistre.getIdContrat();
        log.info("[1] save() entité neuve (id=null) -> INSERT, id attribué par MySQL : {}", contratId);

        // ----- 2. Cascade ALL : les paiements enfants sont insérés avec le contrat -----
        contratEnregistre.getPaiements().addAll(List.of(
                creerPaiement(new BigDecimal("500.00"), contratEnregistre),
                creerPaiement(new BigDecimal("1000.00"), contratEnregistre)));
        // Entité détachée -> save() fait un merge : utiliser l'objet RENVOYÉ,
        // qui porte la collection gérée avec les ids attribués aux paiements.
        Contrat contratAvecPaiements = contratRepository.save(contratEnregistre);
        // java:S2629 : la méthode est calculée AVANT le log (sinon évaluée même si le log est désactivé)
        String idsPaiements = idsPaiements(contratAvecPaiements);
        log.info("[2] save() du contrat avec 2 paiements -> cascade ALL : INSERT des paiements enfants "
                + "(ids attribués : {})", idsPaiements);

        // ----- 3. findById() : SELECT, retourne Optional (entité détachée) -----
        Optional<Contrat> trouve = contratRepository.findById(contratId);
        // java:S2629 : la description est construite AVANT le log
        String contratDesc = trouve
                .map(c -> "Contrat " + c.getIdContrat() + ", total " + c.getMontantTotal()
                        + ", valide=" + c.getValide())
                .orElse("Optional.empty()");
        log.info("[3] findById({}) -> SELECT : {}", contratId, contratDesc);
        log.info("    (la collection LAZY 'paiements' n'est PAS chargée ici : elle ne se charge "
                + "que dans une session -> cf. étape 6)");

        // ----- 4. existsById() et count() -----
        log.info("[4] existsById({}) = {}, count() de contrat = {}", contratId,
                contratRepository.existsById(contratId), contratRepository.count());

        // ----- 5. save() avec id renseigné : merge -> SELECT puis UPDATE si champs modifiés -----
        Contrat contratModifie = trouve.orElseThrow();
        contratModifie.setMontantTotal(new BigDecimal("1800.00"));
        contratRepository.save(contratModifie);
        log.info("[5] save() entité existante (id renseigné) -> merge : SELECT + UPDATE du montantTotal modifié");

        // ----- 6. orphanRemoval : retrait d'un paiement de la liste -> DELETE de l'orphelin -----
        // Pour toucher la collection LAZY, il faut la recharger dans une session (transaction).
        Long paiementRetireId = transactionTemplate.execute(status -> {
            Contrat contratEnSession = contratRepository.findById(contratId).orElseThrow();
            Paiement retire = contratEnSession.getPaiements().remove(0);
            contratRepository.save(contratEnSession);
            log.info("    paiement retiré de la collection : id {}", retire.getIdPaiement());
            return retire.getIdPaiement();
        });
        log.info("[6] Retrait du paiement {} de la liste + save() (en transaction) "
                + "-> orphanRemoval : DELETE du paiement orphelin", paiementRetireId);

        // ----- 7. deleteById() : suppression en cascade des paiements restants -----
        long avantSuppression = contratRepository.count();
        long paiementsRestants = paiementRepository.count();
        contratRepository.deleteById(contratId);
        log.info("[7] deleteById({}) -> cascade REMOVE : contrats {} -> {}, "
                + "paiements {} -> {} (le paiement restant est supprimé avec son contrat)",
                contratId, avantSuppression, contratRepository.count(),
                paiementsRestants, paiementRepository.count());

        // ----- 8. findById() après suppression : Optional.isEmpty() -----
        log.info("[8] findById({}) après suppression -> {}", contratId,
                contratRepository.findById(contratId).isPresent() ? "toujours présent" : "Optional.empty()");

        // ----- 9. deleteById() sur un id inexistant : aucune exception (Spring Data 3) -----
        contratRepository.deleteById(999999L);
        log.info("[9] deleteById(999999) sur une ligne inexistante -> aucune exception levée");

        log.info("========== FIN DE LA DÉMONSTRATION ==========");
    }

    private Paiement creerPaiement(BigDecimal montant, Contrat contrat) {
        Paiement paiement = new Paiement();
        paiement.setMontant(montant);
        paiement.setDatePaiement(LocalDate.now(ZONE));
        paiement.setModePaiement(ModePaiement.VIREMENT);
        paiement.setContrat(contrat);
        return paiement;
    }

    private String idsPaiements(Contrat contrat) {
        return contrat.getPaiements().stream()
                .map(p -> String.valueOf(p.getIdPaiement()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("aucun");
    }
}
