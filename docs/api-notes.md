# Atelier 5 — Notes : API REST, tests Postman et documentation Swagger

Projet : **autoloc-api** (package racine `tn.esprit.autolocn`)
Objectif : exposer les services (Atelier 4) en API REST via Spring MVC, les tester avec Postman, puis les documenter avec SpringDoc / Swagger UI.

---

## A. Endpoints — Tableau

| Contrôleur | Verbe + route | Service appelé | Code de succès | Codes d'erreur |
|---|---|---|---|---|
| `ContratController` | GET /api/contrats | `IContratService.findAll()` | **200** | — |
| `ContratController` | GET /api/contrats/{id} | `IContratService.findById(id)` | **200** | **404** (ResourceNotFoundException) |
| `ContratController` | POST /api/contrats | `IContratService.create(contrat)` | **201** | **400** (IllegalArgumentException / JSON invalide) |
| `ContratController` | PUT /api/contrats/{id} | `IContratService.update(id, contrat)` | **200** | **404**, **400** |
| `ContratController` | DELETE /api/contrats/{id} | `IContratService.deleteById(id)` | **204** | **404** |
| `PaiementController` | GET /api/paiements | `IPaiementService.findAll()` | **200** | — |
| `PaiementController` | GET /api/paiements/{id} | `IPaiementService.findById(id)` | **200** | **404** |

Notes :
- Les routes sont **au pluriel** (`/contrats`, `/paiements`) — les noms de ressource, jamais d'action (`/api/getContrats`).
- Le préfixe `/api` vient du `server.servlet.context-path=/api` ; les `@RequestMapping` des contrôleurs sont relatifs.
- Les 404 viennent de la `ResourceNotFoundException` (traduite par le `GlobalExceptionHandler`) ; les 400 de l'`IllegalArgumentException` (règle de gestion). Le contrôleur ne les produit pas lui-même.

## B. Annotations Jackson — Association, annotation retenue, justification

- **Composition** (`Contrat` ↔ `Paiement`) : `@JsonManagedReference("contrat-paiements")` côté liste (`Contrat.paiements`) et `@JsonBackReference("contrat-paiements")` côté propriétaire (`Paiement.contrat`). La liste (parent) gère, l'enfant se rattache via cascade.
- **Enfant créé seul, désignant son parent par identifiant** (`{"agence": {"id": 1}}`) : `@JsonIgnore` sur la liste côté inverse — l'attribut propriétaire ne reçoit aucune annotation.
- `Reservation.client` : **ne pas utiliser `@JsonBackReference`** (la propriété serait ignorée à la lecture et le POST créerait une réservation sans client).

## C. Postman vs Swagger (ce que chacun apporte)

- **Postman** : client qui envoie des requêtes HTTP et affiche les réponses (statut, en-têtes, corps). Sert à **tester** (requêtes rejouables, cas nominal + cas d'erreur) et à **réaliser les scénarios (cascade Contrat/Paiement)**. Exemple concret : `POST /api/contrats` → 201 (mémoriser `contratId` dans `pm.environment.set("contratId", ...)`), `GET /api/contrats/{{contratId}}` → 200, `DELETE /api/contrats/{{contratId}}` → 204, `GET /api/paiements/{{id}}` → 404 si supprimé.
- **OpenAPI / SpringDoc** : spécification standard qui décrit une API REST (le contrat). SpringDoc la génère en analysant les contrôleurs au démarrage. **Swagger UI** : interface web fournie avec SpringDoc qui présente la documentation et permet d'**essayer** les endpoints (`Try it out` → `Execute`).

## D. Anomalies SonarQube for IDE (étape 12)

Analyse via *Analyze with SonarQube for IDE* sur `web.controller` → **4 anomalies détectées** (2 fichiers).

| Anomalie SonarQube for IDE | Règle / explication | Correction apportée |
|---|---|---|
| `ContratController.findById` (l. 46) renvoie l'entité persistante `Contrat` | `java:S4687` « Replace this persistent entity with a simple POJO or DTO object » — exposer l'entité JPA lie l'API web à la persistance (champs internes `reservation`/`paiements` exposés, couplage aux évolutions du schéma) | Volontaire et provisoire (conformément au PDF) : l'exposition est assumée en Atelier 5 ; correction définitive prévue au **Chapitre 10 (DTO)**. Contrôleurs minces garantis : injection par constructeur via interfaces `I…Service` (`@RequiredArgsConstructor` + `final`) |
| `ContratController.create/update` (l. 51) : `@RequestBody Contrat` — même couplage web ↔ persistance | idem (`java:S4687`) | idem |
| `GlobalExceptionHandler.handleResourceNotFound` (l. 25) : `LocalDateTime.now()` | « Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method » — sans fuseau explicite, l'horodatage dépend du fuseau de la machine (décalages selon l'environnement) | Constante `private static final ZoneId ZONE = ZoneId.systemDefault();` puis `LocalDateTime.now(ZONE)` |
| `GlobalExceptionHandler.handleIllegalArgument` (l. 33) : `LocalDateTime.now()` | idem | idem |

## E. Questions de compréhension

1. Quel composant reçoit toutes les requêtes, et comment retrouve-t-il la méthode du contrôleur à appeler ? → **DispatcherServlet** (via `HandlerMapping`), qui délègue au bon handler par `@RequestMapping`/`@GetMapping`…
2. Pourquoi `ContratController` dépendra-t-il de `IContratService` et non de `ContratServiceImpl` ? → couplage faible (interface) — déjà vu en Atelier 4.
3. Pourquoi un POST renvoie-t-il **201** et un DELETE **204** ? Pourquoi une ressource absente donne-t-elle **404** et non **500** ? → `@ResponseStatus(CREATED/NO_CONTENT)` sur le contrôleur + `GlobalExceptionHandler` (`ResourceNotFoundException` → 404, `IllegalArgumentException` → 400).
4. Pourquoi `PaiementController` n'expose-t-il que des GET ? Quelle réponse reçoit un POST /api/paiements ? → lecture seule : POST → **405 Method Not Allowed**.
5. Quels risques présente l'exposition directe des entités JPA ? Pourquoi `Contrat.paiements` utilise-t-il `@JsonManagedReference` alors que `Agence.vehicules` utilise `@JsonIgnore` ? → boucle de sérialisation infinie (`StackOverflowError`) si bidirectionnelle ; composition vs inversion de non-composition.

## Vérifications finales

- [x] Contrôleurs REST créés (`ContratController`, `PaimController`) — injectés par constructeur via les interfaces `I…Service`
- [x] `GlobalExceptionHandler` créé (404/400 + JSON)
- [x] SpringDoc 3.2.0 + `OpenApiConfig` + Swagger UI
- [x] Tests API via HTTP : GET 200, GET-404, POST 201, POST-400, PUT 200, DELETE 204, POST-paiements 405
- [x] Swagger `/api/v3/api-docs` et `/api/swagger-ui.html` accessibles
- [x] Anomalies SonarQube for IDE corrigées : `ZoneId` explicite ×2 (`GlobalExceptionHandler`) ; `java:S4687` ×2 documentées (DTO prévus au Chapitre 10)

## Livrables attendus

- [x] Entités annotées pour Jackson (composition Contrat↔Paiement) / `@JsonIgnore` ailleurs si nécessaire
- [x] Contrôleurs REST des neuf entités dans `web.controller` (CRUD complet, sauf `PaimController` en lecture seule), injectés par constructeur via les interfaces `I…Service`
- [x] Dépendance SpringDoc, classe `OpenApiConfig` et annotations Swagger ; documentation accessible à `/api/swagger-ui.html` et `/api/v3/api-docs`
- [x] Note `docs/api-notes.md` (sections A–E), commit et push sur le dépôt Git « AutoLoc »

## Critères d'évaluation

- [x] Organisation en couches et contrôleurs minces : `web.controller`, dépendance aux seules interfaces `I…Service`, aucun accès au repository
- [x] Conventions REST : routes au pluriel, verbes adaptés, codes corrects (200, 201, 204, 400, 404, 405)
- [x] Tests Postman (nominaux et cas d'erreur) et documentation Swagger complète : tags, résumés, réponses de succès et d'erreur
- [x] Annotations Jackson pertinentes sans boucle de sérialisation ; observations exactes sur la cascade et la lecture seule de Paiement
- [x] Exploitation de SonarQube for IDE : **4 anomalies relevées** sur `web.controller` (`java:S4687` ×2 + fuseau horaire ×2), 2 corrigées immédiatement et 2 documentées (DTO au Chapitre 10)
