# Atelier 4 — Notes : couche Service, IoC et injection de dépendances

Projet : **autoloc-api** (package racine `tn.esprit.autolocn`)
Objectif : construire la couche `service` (logique métier) et comprendre l'Inversion de Contrôle (IoC) et l'injection de dépendances (DI).

---

## A. Modes d'injection comparés (étape 2)

Expérience : dans `ContratServiceImpl`, remplacement temporaire de `@RequiredArgsConstructor` + champ `final` par une injection par attribut `@Autowired private`.

| Critère | Constructeur (`@RequiredArgsConstructor` + `final`) | Attribut (`@Autowired private`) |
|---|---|---|
| Champ `final` possible ? | **Oui** — la dépendance est fixée à la construction | **Non** — le conteneur écrit après coup |
| Dépendance visible dans la signature de la classe ? | **Oui** — le constructeur expose ses dépendances | **Non** — cachée dans le corps de la classe |
| Classe utilisable avec `new` hors conteneur sans dépendance null ? | **Oui** — on fournit le repository au constructeur | **Non** — la dépendance reste `null` (invoquable qu'avec le conteneur) |
| Signalé par SonarQube for IDE ? | Non | **Oui — règle `S6813`** (injection par attribut déconseillée) |
| Objet créable dans un état incomplet ? | **Jamais** | Oui (entre l'instanciation et l'injection) |

**Conclusion (convention AutoLoc)** : injection par constructeur, champs `final`, dépendance typée par une interface. Lombok `@RequiredArgsConstructor` génère le constructeur des champs `final` ; Spring, constatant **un constructeur unique**, l'utilise pour l'injection **sans `@Autowired`**.

---

## B. Les neuf services (package `tn.esprit.autolocn.service`)

| Service | Dépendances injectées | Mode d'injection | Justification |
|---|---|---|---|
| `ContratServiceImpl` | `IContratRepository` | Constructeur (champ `final`) | Fil rouge : logique métier du contrat + garde sur l'id et le montant total |
| `AgenceServiceImpl` | `IAgenceRepository` | Constructeur (champ `final`) | CRUD complet, règle sur le nom obligatoire |
| `EmployeServiceImpl` | `IEmployeRepository` | Constructeur (champ `final`) | CRUD complet, nom et prénom obligatoires |
| `VehiculeServiceImpl` | `IVehiculeRepository` | Constructeur (champ `final`) | CRUD complet, tarif journalier non négatif |
| `EquipementServiceImpl` | `IEquipementRepository` | Constructeur (champ `final`) | CRUD complet, libellé obligatoire |
| `ClientServiceImpl` | `IClientRepository` | Constructeur (champ `final`) | CRUD complet, nom et e-mail obligatoires |
| `ReservationServiceImpl` | `IReservationRepository` | Constructeur (champ `final`) | CRUD complet, date de fin >= date de début |
| `PaiementServiceImpl` | `IPaiementRepository` | Constructeur (champ `final`) | **Lecture seule** : `findById`/`findAll` uniquement — les écritures passent par le Contrat (composition) |
| `MaintenanceServiceImpl` | `IMaintenanceRepository` | Constructeur (champ `final`) | CRUD complet, date de fin >= date de début |

Points communs :
- Tous annotés `@Service` + `@RequiredArgsConstructor`, aucun attribut modifiable (**service sans état** — singleton partagé par les threads).
- Aucun `new` de repository, aucune injection par attribut, dépendance toujours typée par **interface**.
- `create` refuse un id non null (sinon `save()` déclencherait un `merge` et écraserait une ligne existante).
- `update` **charge l'existant puis recopie** les attributs modifiables (passer l'objet reçu à `save()` écraserait les colonnes et, avec `orphanRemoval`, pourrait vider les paiements).
- `findById`/`deleteById` lèvent `ResourceNotFoundException` (l'absence devient une erreur métier explicite ; `deleteById` du repository ne lève rien).
- Règles de gestion via `IllegalArgumentException` avec message clair (validation déclarative au Chapitre 10), implémentées sur les **attributs réellement présents** dans le modèle.

---

## C. Messages d'erreur du conteneur (étape 3)

### Message 1 — `required a bean of type 'IContratService' that could not be found`
**Exception** : `NoSuchBeanDefinitionException` (enveloppée par `UnsatisfiedDependencyException`).
**Deux causes probables côté `ContratServiceImpl`** :
1. **Annotation oubliée** : `@Service` absent → la classe n'est pas une définition de bean.
2. **Hors du package de scan** : classe placée en dehors de `tn.esprit.autolocn` → jamais détectée (le scan ne couvre que le package racine et ses sous-packages).

**Correction** : ajouter `@Service` (ou `@Component`) et/ou vérifier que la classe est dans le bon package.

### Message 2 — `required a single bean, but 2 were found: emailNotificateur, smsNotificateur`
**Exception** : `NoUniqueBeanDefinitionException`.
**Deux corrections** :
1. **`@Primary` posé sur la CLASSE** d'un des deux beans (ex. `@Primary class EmailNotificateur`) → ce bean devient le candidat par défaut.
2. **`@Qualifier("emailNotificateur")` posé à l'injection**, c'est-à-dire sur le **paramètre du constructeur** (ex. `NotificationController(…, @Qualifier("emailNotificateur") Notificateur n)`) → choix explicite du bean nommé.

### Message 3 — cycle `clientServiceImpl ↔ reservationServiceImpl`
**Exception** : `BeanCurrentlyInCreationException` (dépendance circulaire).
**Pourquoi l'injection par constructeur rend le cycle impossible à contourner** : chaque bean ne peut être instancié qu'**après** ses dépendances ; dans un cycle, `clientServiceImpl` attend `reservationServiceImpl` qui attend `clientServiceImpl` — **aucun des deux ne peut être créé**, le démarrage échoue. (Une injection par attribut masquerait le problème en laissant des instances partiellement construites, ce qui est pire.)
**Piste de conception pour le supprimer** : extraire la responsabilité commune dans un **troisième service** (lequel ne dépend d'aucun des deux), ou revoir les responsabilités pour supprimer le lien mutuel (un service ne doit jamais avoir besoin de l'autre).

> Astuce de lecture d'une longue trace : lire d'abord la rubrique **Description** ou la dernière ligne **Caused by** — c'est la cause réelle ; les premières lignes ne font qu'enrober l'erreur.

---

## D. Anomalies SonarQube for IDE (étape 4)

Analyse réalisée via *Analyze with SonarQube for IDE* sur les packages `service` et `exception`.

| Anomalie SonarQube for IDE | Règle / explication | Correction apportée |
|---|---|---|
| Injection par attribut `@Autowired private IContratRepository` (expérience de l'étape 2) | `S6813` — l'injection par attribut est déconseillée : champ non `final`, dépendances cachées, `null` hors conteneur | Restauration de l'injection par constructeur : `@RequiredArgsConstructor` + champ `final` |
| `throw new RuntimeException(...)` dans `findById` (expérience de l'étape 4.2) | `S112` — une exception générique ne doit pas être levée (impossible à cibler ni à traiter spécifiquement) | Restauration de `ResourceNotFoundException`, l'exception métier |
| Test `AutolocnApplicationTests.contextLoads()` (package test) | `java:S2699` — un test ne doit pas être un simple test de conteneur sans assertion | Ajouter une assertion d'intégration (ex. `assertThat(context).isNotNull()`) ou retirer le test trivial |

---

## E. Questions de compréhension

**1. Où est le `new` de `ContratServiceImpl` et qui l'exécute ?**
Il n'existe **aucun `new` dans le code source** : c'est le **conteneur Spring IoC** (`ApplicationContext`, créé par `SpringApplication.run(...)`) qui l'exécute au démarrage, à la phase *Instanciation*, en appelant le constructeur généré par `@RequiredArgsConstructor` — puis il fournit le `IContratRepository` (phase *Injection*).

**2. Pourquoi un contrôleur dépendra-t-il de `IContratService` et non de `ContratServiceImpl` ?**
Pour le **couplage faible** : chaque couche dépend de l'**interface** de la couche inférieure, jamais de son implémentation. On peut remplacer l'implémentation (tests avec un mock, autre version) sans modifier le contrôleur ; c'est le principe de dépendance vers l'abstraction (DIP).

**3. Pourquoi un service singleton doit-il rester sans état ? Exemple de bug.**
Le scope `singleton` (défaut) : **une seule instance partagée par tous les threads**. Si elle portait un attribut modifiable — ex. un champ `contratCourant` — deux requêtes simultanées l'écriraient mutuellement (condition de course) : l'utilisateur A crée un contrat, l'utilisateur B crée le sien, `contratCourant` pointe vers celui de B et l'enregistrement de A se fait dans le contrat de B → **données corrompues**.

**4. Pourquoi `update` charge-t-il l'entité existante avant `save` au lieu de passer directement l'objet reçu ?**
Un `save()` sur l'objet reçu déclenche un `merge` qui mettrait à jour **toutes les colonnes** à partir d'un objet potentiellement incomplet (champs à `null`), et, avec `orphanRemoval`, une collection absente/vidée supprimerait les **paiements orphelins**. Charger l'existant puis **recopier uniquement les attributs modifiables** préserve l'identifiant, les associations et la collection.

**5. `@Component` vs `@Bean` ? `@Primary` vs `@Qualifier` ? Pourquoi `IPaiementService` sans écriture ?**
- **`@Component`** : stéréotype posé **sur la classe** — le scan du package racine en fait un bean (classe que l'on peut annoter). **`@Bean`** : déclaration explicite **par une méthode** d'une classe `@Configuration` — pour une classe qu'on ne peut pas annoter (bibliothèque, classe du JDK) ou une construction particulière.
- **`@Primary`** : posé **sur la classe** — désigne le candidat par défaut si plusieurs beans du même type. **`@Qualifier("nom")`** : posé **à l'injection** (paramètre/champ) — choix explicite d'un bean nommé.
- **`IPaiementService`** est en lecture seule car `Paiement` est un **enfant de `Contrat` (composition)** : créer/modifier/supprimer un paiement doit passer par le Contrat (cascade `ALL` + `orphanRemoval`), sinon on risque des paiements orphelins.

---

## Vérifications finales

- [x] Package `exception` : `ResourceNotFoundException`
- [x] 9 interfaces `I…Service` + 9 implémentations `…ServiceImpl` dans `service`
- [x] Injection par constructeur (champ `final`), aucun `@Autowired` sur attribut, aucun `new` de bean
- [x] Règles de gestion adaptées au modèle réel (attributs Atelier 2)
- [x] Démarrage de l'application sans erreur (log `Found 9 JPA repository interfaces`)
- [x] Étape 2 : expérience injection par attribut notée (tableau A, S6813) puis code restauré
- [x] Étape 4 : 3 anomalies relevées et corrigées (tableau D : S6813, S112, S2699)
- [ ] Commit « Atelier 4 : couche Service, IoC et injection de dépendances » + push
