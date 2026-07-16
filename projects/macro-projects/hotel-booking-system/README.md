# Hotel Booking System

Systeme de reservation hoteliere : gestion des chambres, des clients et des reservations,
avec detection de chevauchement de dates et calcul automatique du prix total.

## But

Simuler le coeur metier d'un moteur de reservation d'hotel : empecher les doubles reservations
sur une meme chambre, calculer le prix d'un sejour, et liberer une chambre a l'annulation.
Projet de la bibliotheque d'ingenierie personnelle, palier "macro" (application multi-couches
complete, hors microservices).

## Architecture

Application Spring Boot monolithique en couches classiques :

```
controller/   Endpoints REST (Room, Guest, Booking) - validation d'entree, pas de logique metier
service/      Logique metier : chevauchement de dates, calcul de prix, regles de creation/annulation
repository/   Spring Data JPA (interfaces uniquement)
entity/       Room, Guest, Booking + enums RoomType, BookingStatus
dto/          BookingRequest (entree API dediee, distincte de l'entite Booking)
exception/    Exceptions metier + @RestControllerAdvice pour les reponses d'erreur JSON
```

Frontend : une seule page statique (`src/main/resources/static/index.html`) servie directement
par Spring, qui appelle l'API REST en `fetch`. Choix assume : pas d'Angular CLI ni de build
frontend separe pour ce palier de projet — une page HTML/JS suffit a demontrer les flux.

Persistance : H2 en fichier (`./data/hotel-booking-db`), donc les donnees survivent aux
redemarrages de l'application (contrairement a une H2 en memoire).

## Regles metier

- **Chevauchement de dates** : une chambre ne peut pas etre reservee si la periode demandee
  chevauche une reservation active (statut != CANCELLED) existante sur cette chambre.
  Intervalles semi-ouverts `[checkIn, checkOut)` : un checkout et un checkin le meme jour ne
  sont pas consideres comme un conflit. Voir `BookingService.datesOverlap`.
- **Calcul du prix** : `prix total = nombre de nuits x prix/nuit de la chambre`. Le prix/nuit
  est propre a chaque chambre (donc indirectement au type de chambre). Voir
  `BookingService.calculateTotalPrice`.
- **Annulation** : passe la reservation en statut `CANCELLED`, ce qui la retire automatiquement
  du calcul de disponibilite (aucune suppression physique, pour garder l'historique).

## Lancer le projet

Prerequis : Java 17, Maven.

```bash
mvn spring-boot:run
```

L'application demarre sur `http://localhost:8081`. Page de demo : `http://localhost:8081/`.
Console H2 (debug) : `http://localhost:8081/h2-console` (JDBC URL : `jdbc:h2:file:./data/hotel-booking-db`).

## Build et tests

```bash
mvn compile
mvn test
```

## Endpoints principaux

| Methode | URL | Description |
|---|---|---|
| POST | `/api/rooms` | Creer une chambre |
| GET | `/api/rooms` | Lister toutes les chambres |
| GET | `/api/rooms/{id}` | Detail d'une chambre |
| GET | `/api/rooms/available?checkIn=...&checkOut=...` | Chambres disponibles sur une periode |
| POST | `/api/guests` | Creer un client |
| GET | `/api/guests` | Lister les clients |
| POST | `/api/bookings` | Creer une reservation (`roomId`, `guestId`, `checkInDate`, `checkOutDate`) |
| GET | `/api/bookings` | Lister toutes les reservations |
| GET | `/api/bookings/{id}` | Detail d'une reservation |
| GET | `/api/bookings/guest/{guestId}` | Reservations d'un client |
| POST | `/api/bookings/{id}/cancel` | Annuler une reservation (libere la chambre) |

## Limitations connues

- Pas d'authentification/autorisation : tous les endpoints sont ouverts (hors perimetre de ce
  palier, focus sur la logique metier de reservation).
- Pas de gestion de tarification dynamique (saisons, remises, taxes) : le prix est un simple
  `nuits x prix/nuit`.
- Pas de surbooking volontaire ni de liste d'attente.
- La page HTML de demo est volontairement minimale (pas de framework JS, pas de pagination).
- Aucune notion de paiement : une reservation `CONFIRMED` n'implique pas de transaction financiere.
