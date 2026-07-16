# hotel-booking-system

Systeme de reservation hoteliere (Room / Guest / Booking). Voir [README.md](README.md) pour
le detail fonctionnel et les endpoints.

## Modules

| Package | Role |
|---|---|
| `controller` | Endpoints REST : RoomController, GuestController, BookingController |
| `service` | Logique metier : BookingService (chevauchement, prix, annulation), RoomService, GuestService |
| `repository` | Interfaces Spring Data JPA : RoomRepository, GuestRepository, BookingRepository |
| `entity` | Room, Guest, Booking, enums RoomType et BookingStatus |
| `dto` | BookingRequest (payload d'entree pour POST /api/bookings) |
| `exception` | Exceptions metier + GlobalExceptionHandler (@RestControllerAdvice) |

## Stack (verifiee)

- Spring Boot 3.2.5, Java 17, groupId `com.jihedapps`
- Spring Web, Spring Data JPA, Spring Validation, Thymeleaf (starter present, pas de vue Thymeleaf active)
- H2 embarquee en fichier (`./data/hotel-booking-db`), pas de PostgreSQL
- Frontend : page HTML/JS statique unique servie par Spring (pas d'Angular CLI)
- Tests : JUnit 5 + Mockito (via spring-boot-starter-test)

## Commandes

```bash
mvn compile          # compilation
mvn test             # tests unitaires (BookingServiceTest : chevauchement + prix + workflow)
mvn spring-boot:run  # lancer sur http://localhost:8081
```

## Regles specifiques au projet

- Toute la logique de chevauchement de dates et de calcul de prix vit dans `BookingService`
  sous forme de methodes statiques testables (`datesOverlap`, `calculateTotalPrice`) : ne pas
  la deplacer dans le controller ni dans l'entite.
- Une reservation n'est jamais supprimee physiquement : l'annulation change le statut en
  `CANCELLED`. Toute nouvelle regle de disponibilite doit filtrer sur le statut, pas sur
  l'existence de la ligne.
- Port dedie 8081 (le projet `helpdesk-ticket-system` voisin utilise un port different pour
  permettre de lancer les deux en parallele si besoin).
