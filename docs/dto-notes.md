# Activité DTO — Notes : DTO et MapStruct (AutoLoc)

Projet : **autoloc-api** (package racine `tn.esprit.autolocn`)
Objectif : séparer les données exposées par l'API des entités JPA grâce aux **DTO** (Data Transfer Objects) — contrôle des données **reçues** (`*RequestDto`) et personnalisation des données **renvoyées** (`*ResponseDto`), avec **MapStruct** pour les conversions.

---

## A. Pourquoi les DTO ?

Sans DTO, le contrôleur accepte et renvoie directement les entités JPA :

- **Couplage** : le contrat JSON dépend du schéma JPA (tout changement d'entité casse l'API) ;
- **Fuite de données** : tous les champs sont exposés (ex. e-mail, permis, réservations) ;
- **Lazy-loading** : des objets persistants transitent hors de leur transaction.

> C'est la correction de l'anomalie SonarQube `java:S4687` (« Replace this persistent entity with a simple POJO or DTO object ») repérée à l'Atelier 5 sur `ContratController`.

| Rôle | DTO | Contenu |
|---|---|---|
| **Reçu** (POST) | `ClientRequestDto` | nom, prénom (**obligatoires**, `@NotBlank`), e-mail, téléphone (facultatifs) |
| **Renvoyé** (GET) | `ClientResponseDto` | id, nom, prénom, téléphone — **jamais l'e-mail** |
| **Renvoyé** (GET) | `ReservationResponseDto` | réservation **aplatie** : id, dates, statut + nom/prénom client + marque/modèle/immatriculation véhicule |

## B. MapStruct (Phase 0)

- Dépendance `org.mapstruct:mapstruct` **1.6.3** (dernière stable) + `mapstruct-processor` dans les `annotationProcessorPaths` du `maven-compiler-plugin` ;
- **Ordre obligatoire** : `lombok` → `org.projectlombok:lombok-mapstruct-binding:0.2.0` → `mapstruct-processor` (le pont permet à MapStruct de voir les getters/setters générés par Lombok) ;
- `@Mapper(componentModel = "spring")` : MapStruct génère `ClientMapperImpl` / `ReservationMapperImpl`, exposés comme beans Spring → injectés par constructeur dans les services.

```java
@Mapper(componentModel = "spring")
public interface ClientMapper {
    Client toEntity(ClientRequestDto dto);            // DTO reçu -> entité JPA
    ClientResponseDto toResponseDto(Client client);   // entité JPA -> DTO renvoyé
}
```

## C. Scénario 1 — Gestion des clients

### Artefacts

| Fichier | Rôle |
|---|---|
| `dto/ClientRequestDto.java` | DTO de réception, `@NotBlank` sur nom/prénom |
| `dto/ClientResponseDto.java` | DTO de réponse (sans e-mail) |
| `mapper/ClientMapper.java` | mappers MapStruct (généré : `ClientMapperImpl`) |
| `service/IClientService.java` + `ClientServiceImpl.java` | `create(ClientRequestDto)` → `ClientResponseDto`, `findByIdDto(Long)` → `ClientResponseDto` |
| `web/controller/ClientController.java` | `POST /api/clients` (201), `GET /api/clients/{id}` (200) |

### Adaptations nécessaires

1. **Entité `Client` assouplie** : `email`, `telephone`, `numPermis` passent de `nullable = false` à nullable (MySQL autorise plusieurs `NULL` dans une colonne `UNIQUE`) — sinon un client sans e-mail est impossible ;
2. **Base `autoloc`** : `ddl-auto=update` ne retire pas les contraintes `NOT NULL` → `ALTER TABLE client MODIFY email varchar(100) NULL, MODIFY telephone varchar(20) NULL, MODIFY num_permis varchar(30) NULL;` ;
3. **Validation service** : « nom + e-mail obligatoires » → « **nom + prénom** obligatoires » (cohérent avec le DTO) ;
4. **`dateInscription`** (NOT NULL, non reçu via le DTO) : renseignée par le service avec `LocalDate.now(ZONE)` (`ZoneId` explicite — règle SonarQube) ;
5. **`GlobalExceptionHandler`** : nouveau handler `MethodArgumentNotValidException` → **400** au même format JSON que l'Atelier 5 (`statut` / `message` / `horodatage`).

## D. Scénario 2 — Consultation des réservations

| Fichier | Rôle |
|---|---|
| `dto/ReservationResponseDto.java` | réservation aplatie (valeurs simples uniquement) |
| `mapper/ReservationMapper.java` | `@Mapping(source = "client.nom", target = "nomClient")` etc. |
| `service/IReservationService.java` + `ReservationServiceImpl.java` | `findAll()` → `List<ReservationResponseDto>`, nouvelle `findByClientId(Long)` |
| `repository/IReservationRepository.java` | `List<Reservation> findByClient_IdClient(Long)` |
| `web/controller/ReservationController.java` | `GET /api/reservations`, `GET /api/reservations/client/{idClient}` |

Points clés :

- **Mapping imbriqué aplati** : les 5 `@Mapping` transforment `client.nom/prénom` et `vehicule.marque/modèle/immatriculation` en champs scalaires — **aucun objet `Client` ou `Vehicule` dans le JSON** ;
- **Lazy-loading maîtrisé** : les méthodes DTO du service sont `@Transactional(readOnly = true)` — les associations LAZY doivent être lues dans la transaction (sinon `LazyInitializationException`) ;
- **Repository dérivé** : le chemin est `client.idClient` (nom du champ), d'où `findByClient_IdClient` (Spring Data refuse `client.id` → `No property 'id' found for type 'Client'`).

## E. Tests réalisés (7/7 OK)

Base : `http://localhost:8089/api` — script Python (urllib) exécuté en 2 phases.

### Scénario 1 — clients

| # | Test | Résultat |
|---|---|---|
| T1 | `POST /clients` avec toutes les informations | **201** `{"idClient":1,"nom":"Ben Amor","prenom":"Sonia","telephone":"98 111 222"}` — pas d'e-mail ✓ |
| T2 | `POST /clients` sans e-mail ni téléphone | **201** `{"idClient":2,...,"telephone":null}` ✓ |
| T3 | `POST /clients` sans nom (bonus) | **400** `{"statut":400,"message":"nom : Le nom du client est obligatoire","horodatage":"..."}` ✓ |
| T4 | `GET /clients/1` | **200** sans e-mail dans la réponse ✓ |

### Scénario 2 — réservations (3 lignes insérées en SQL)

| # | Test | Résultat |
|---|---|---|
| T5 | `GET /reservations` | **200**, 3 lignes, **toutes plates** : `{"idReservation":1,"dateDebut":"2026-12-01","dateFin":"2026-12-05","statut":"CONFIRMEE","nomClient":"Ben Amor","prenomClient":"Sonia","marque":"Peugeot","modele":"208","immatriculation":"7890-TN-456"}` — aucun champ `client`/`vehicule` ✓ |
| T6 | `GET /reservations/client/1` | **200**, 2 réservations filtrées (Ben Amor), plates ✓ |
| T7 | `GET /reservations/client/9999` | **200** `[]` ✓ |

### Vérification négative (JSON sans entités imbriquées)

Le JSON T5 contient bien `nomClient`, `prenomClient`, `marque`, `modele`, `immatriculation` en scalaires — le test automatique vérifie l'absence des clés `client` et `vehicule` sur **chaque** ligne (`flat=True`).

## Vérifications finales

- [x] MapStruct 1.6.3 + Lombok : compilation OK, `ClientMapperImpl` / `ReservationMapperImpl` générés
- [x] `ClientRequestDto` / `ClientResponseDto` / `ClientMapper` / `ClientController` (POST 201 + GET 200)
- [x] Validation `@Valid` → 400 JSON via `GlobalExceptionHandler` (format Atelier 5)
- [x] Entité `Client` + base MySQL : e-mail/téléphone/permis facultatifs (`ALTER TABLE`)
- [x] `ReservationResponseDto` / `ReservationMapper` / `ReservationController` (2 GET)
- [x] Tests : **7/7 OK** (client complet, sans e-mail/téléphone, 400, GET par id, liste, par client, vide)
- [x] JSON de réservation **aplati** — aucune entité `Client`/`Vehicule` imbriquée

## Livrables

- [x] Phase 0 : MapStruct dans le `pom.xml`
- [x] Phase 1 : scénario 1 clients (DTO, mapper, service, contrôleur, validation)
- [x] Phase 2 : scénario 2 réservations (DTO aplati, mapper, service, contrôleur)
- [x] Phase 3 : tests HTTP (7/7)
- [x] Phase 4 : cette note — commit et push sur le dépôt Git « AutoLoc »
