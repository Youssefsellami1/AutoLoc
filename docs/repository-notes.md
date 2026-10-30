# Atelier 3 — Notes : couche Repository Spring Data JPA

Projet : **autoloc-api** (package racine `tn.esprit.autolocn`)
Objectif : mettre en place la couche `repository` (9 interfaces Spring Data JPA) et maîtriser les opérations CRUD.

## 1. Choix de l'interface repository

Hiérarchie des interfaces Spring Data JPA (de la plus minimale à la plus complète) :

```
Repository<T, ID>                          interface marqueur, aucune méthode
 ├── CrudRepository<T, ID>                 CRUD de base (findAll renvoie Iterable)
 │    └── ListCrudRepository<T, ID>        idem, collections renvoyées en List
 └── PagingAndSortingRepository<T, ID>     tri et pagination
      └── ListPagingAndSortingRepository   idem, collections renvoyées en List

JpaRepository<T, ID>  =  ListCrudRepository
                       + ListPagingAndSortingRepository
                       + QueryByExampleExecutor
                       + méthodes propres à JPA (flush, suppressions en lot…)
```

> Depuis Spring Data 3, `PagingAndSortingRepository` n'étend plus `CrudRepository` :
> il faut déclarer les deux, ou étendre `JpaRepository` qui les réunit déjà.

**Interface retenue pour AutoLoc : `JpaRepository<Entité, Long>`** — CRUD complet,
`findAll` renvoie une `List`, tri/pagination disponibles, `saveAndFlush` disponible.
L'atelier démarre avec `CrudRepository` sur `Contrat` (étape 1) pour observer le
comportement de base, puis migre vers `JpaRepository` (étape 2).

## 2. Les neuf interfaces du package `tn.esprit.autolocn.repository`

| Interface | Étend | Justification |
|---|---|---|
| `IAgenceRepository` | `JpaRepository<Agence, Long>` | CRUD complet, listes, tri et pagination pour la lecture des agences. |
| `IEmployeRepository` | `JpaRepository<Employe, Long>` | CRUD complet, listes, tri et pagination. |
| `IVehiculeRepository` | `JpaRepository<Vehicule, Long>` | CRUD complet ; le tri (`Sort`) servira pour les recherches de véhicules. |
| `IEquipementRepository` | `JpaRepository<Equipement, Long>` | CRUD complet, listes, tri et pagination. |
| `IClientRepository` | `JpaRepository<Client, Long>` | CRUD complet, listes, tri et pagination. |
| `IReservationRepository` | `JpaRepository<Reservation, Long>` | CRUD complet ; pagination (`Pageable`) utile pour l'historique des réservations. |
| `IContratRepository` | `JpaRepository<Contrat, Long>` | Fil rouge : CRUD complet, `findAll` renvoie une `List`, `saveAndFlush` disponible. |
| `IPaiementRepository` | `JpaRepository<Paiement, Long>` | Lecture seule des paiements : les écritures passent par le `Contrat` (composition, cascade + orphanRemoval). |
| `IMaintenanceRepository` | `JpaRepository<Maintenance, Long>` | CRUD complet, listes, tri et pagination. |

Points retenus :
- Convention de nommage : chaque interface commence par le préfixe **`I`** (`IVehiculeRepository`, …).
- Une repository est une **simple interface** : Spring Data génère le proxy à l'exécution ; l'annotation `@Repository` n'est pas nécessaire.
- En composition, l'enfant (`Paiement`) n'est modifié qu'à travers son parent (`Contrat`) ; `IPaiementRepository` sert uniquement à lire.
- Les suppressions « en lot » (`deleteAllInBatch`, `deleteAllByIdInBatch`) **contournent** le contexte de persistance : cascade et `orphanRemoval` ne s'appliquent pas → jamais sur `Contrat` (paiements orphelins).

## 3. Comportement du CRUD observé (programme de démonstration)

Le programme `tn.esprit.autolocn.demo.Atelier3Demo` (activation : `--autoloc.atelier3.demo=true`)
observe, avec les logs SQL, le comportement suivant sur `Contrat` ↔ `Paiement` :

| Situation | Opération JPA déclenchée |
|---|---|
| `save()` avec id **null** (entité neuve) | `persist` → **INSERT** ; l'id est attribué par MySQL (IDENTITY) et renseigné dans l'objet retourné. |
| `save()` avec id **renseigné** (entité existante) | `merge` → **SELECT** de la ligne, puis **UPDATE** si des champs ont changé. Utiliser l'objet retourné. |
| `save()` d'un contrat avec ses paiements | Cascade `ALL` : INSERT du contrat **et** de ses paiements enfants. |
| Retrait d'un `Paiement` de la liste + `save()` | `orphanRemoval` : **DELETE** du paiement orphelin. |
| `deleteById()` | Passage par le contexte de persistance : cascade `REMOVE` appliquée (contrat **et** paiements restants supprimés). |
| `deleteById()` sur un id inexistant | Aucune exception (Spring Data 3). |
| `findById()` | `SELECT` ; retourne `Optional<T>` (à traiter : `isPresent()`, `orElseThrow()`…). |

## 4. Anomalies SonarQube for IDE (SonarLint)

Analyse réalisée via *Analyze with SonarQube for IDE* (clic droit sur le projet).
Résultat : **4 anomalies détectées**, toutes dans `Atelier3Demo.java` (le reste du projet : aucun problème). Toutes corrigées :

| Anomalie SonarQube for IDE | Règle / explication | Correction apportée |
|---|---|---|
| `LocalDate.now()` dans `contrat.setDateSignature(...)` (niveau info) | « Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method » — sans fuseau explicite, la date obtenue dépend du fuseau de la machine, source de décalages (lendemain/veille) selon l'environnement | Constante `private static final ZoneId ZONE = ZoneId.systemDefault();` puis `LocalDate.now(ZONE)` |
| `LocalDate.now()` dans `paiement.setDatePaiement(...)` (niveau info) | Même règle que ci-dessus | Idem : `LocalDate.now(ZONE)` |
| Appel `idsPaiements(contratAvecPaiements)` **dans** le `log.info` de l'étape 2 (l. 77) | `java:S2629` « Invoke method(s) only conditionally » — les arguments d'un log sont évalués **même si le niveau de log est désactivé** : calcul inutile à chaque exécution | Extraction de l'appel dans une variable locale `idsPaiements` **avant** le `log.info` |
| Expression `trouve.map(...).orElse(...)` **dans** le `log.info` de l'étape 3 (l. 82) | `java:S2629` — même problème : construction de chaîne + appels de méthodes exécutés même sans log actif | Extraction dans une variable locale `contratDesc` **avant** le `log.info` |

> Leçons retenues : (1) toujours passer un `ZoneId` (ou un `Clock`) aux méthodes `.now()` de `java.time` ;
> (2) ne jamais appeler de méthode dont le résultat sert uniquement au log — la calculer avant, ou protéger
> l'appel par `if (log.isXxxEnabled())`.

## 5. Vérifications finales

- [x] Neuf interfaces `I…Repository` dans le package `repository`, toutes `JpaRepository<Entité, Long>`
- [ ] Démarrage de l'application sans erreur ni nouvelle instruction DDL
- [ ] Log `Found 9 JPA repository interfaces` au démarrage
- [x] Anomalies SonarQube for IDE relevées et corrigées (4 anomalies, tableau ci-dessus)
- [ ] Commit et push : « Atelier 3 : couche Repository Spring Data JPA »
