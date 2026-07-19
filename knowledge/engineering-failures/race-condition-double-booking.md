# Double réservation (race condition sur une vérification de disponibilité)

> Deux requêtes concurrentes vérifient chacune "cette ressource est-elle libre ?", obtiennent
> toutes les deux "oui", et réservent toutes les deux — la vérification et la réservation ne sont
> pas atomiques, donc rien n'empêche les deux requêtes de passer entre les deux.

## 🔍 Cause

Le pattern classique et fautif : `SELECT` pour vérifier la disponibilité, puis `INSERT`/`UPDATE`
pour réserver, dans deux requêtes SQL séparées sans verrou entre les deux.

```java
// Fautif : fenetre de course entre le check et l'ecriture
if (roomRepository.isAvailable(roomId, checkIn, checkOut)) {   // (1) lecture
    bookingRepository.save(new Booking(roomId, checkIn, checkOut));  // (2) ecriture
}
```
Si deux requêtes pour la même chambre et les mêmes dates arrivent à quelques millisecondes
d'intervalle, les deux exécutent l'étape (1) avant qu'aucune n'ait exécuté l'étape (2) — les deux
voient la chambre libre, les deux réservent. Sous faible trafic, cette fenêtre de quelques
millisecondes ne se déclenche presque jamais en test manuel, ce qui explique pourquoi le bug
survit souvent longtemps avant d'être détecté : il ne se manifeste qu'à partir d'un vrai niveau de
concurrence, typiquement en production sous charge, jamais en dev où les requêtes sont séquentielles
de fait.

## 🚨 Symptômes

- Deux réservations confirmées pour la même ressource, les mêmes dates, visibles côté client des
  deux utilisateurs — découvert généralement par un client qui se présente pour trouver sa
  réservation "déjà prise", pas par une alerte technique.
- Aucune erreur applicative dans les logs — les deux requêtes ont réussi individuellement, chacune
  a fait exactement ce qu'on lui demandait. Le bug n'est pas dans une des deux requêtes, il est
  dans l'absence d'atomicité entre elles.
- Corrélation temporelle : les deux réservations en conflit ont des timestamps très proches
  (souvent sous la seconde) — signature typique d'une race condition plutôt que d'un bug de logique
  métier classique.

## 🩺 Comment diagnostiquer

Repérer les doublons a posteriori :
```sql
SELECT room_id, check_in, check_out, COUNT(*), array_agg(id) as booking_ids
FROM bookings
GROUP BY room_id, check_in, check_out
HAVING COUNT(*) > 1;
```
Comparer les timestamps de création des deux réservations en conflit — un écart de quelques
millisecondes à quelques dizaines de millisecondes confirme la race condition plutôt qu'une erreur
de logique (qui produirait des doublons à n'importe quel écart de temps, pas seulement des écarts
très courts).

## ✅ Solution

Trois approches valables, à choisir selon la contrainte réelle :
- **Contrainte d'exclusion en base** (la plus robuste) : laisser la base garantir l'invariant
  plutôt que de compter sur l'application pour ne jamais se tromper. PostgreSQL avec l'extension
  `btree_gist` permet une contrainte d'exclusion sur les plages de dates qui se chevauchent :
```sql
ALTER TABLE bookings ADD CONSTRAINT no_overlapping_bookings
EXCLUDE USING gist (room_id WITH =, daterange(check_in, check_out) WITH &&);
```
  Une tentative de double réservation échoue alors au niveau base avec une violation de contrainte,
  peu importe combien de requêtes concurrentes arrivent en même temps — c'est la base qui applique
  l'atomicité, pas l'application.
- **Verrou pessimiste** (`SELECT ... FOR UPDATE`) sur la ligne de la ressource pendant la
  transaction de vérification+réservation — plus simple à comprendre mais sérialise l'accès à
  cette ressource précise, ce qui devient un goulot d'étranglement si une même ressource est très
  demandée en même temps (forte contention).
- **Verrou optimiste** (colonne de version, `@Version` en JPA) si les conflits sont rares en
  pratique : chaque transaction échoue et retente si la version a changé entre lecture et
  écriture, sans jamais bloquer d'autres transactions en attente.

```java
@Transactional
public Booking book(Long roomId, LocalDate checkIn, LocalDate checkOut) {
    // le verrou est pose sur la ligne room, pas sur booking - toute tentative concurrente
    // de reservation sur la meme chambre attend la fin de cette transaction
    Room room = roomRepository.findByIdForUpdate(roomId);
    if (!bookingRepository.isAvailable(roomId, checkIn, checkOut)) {
        throw new RoomUnavailableException(roomId, checkIn, checkOut);
    }
    return bookingRepository.save(new Booking(room, checkIn, checkOut));
}
```

## 🛡️ Prévention

- Ne jamais faire confiance à un `SELECT` de disponibilité suivi d'un `INSERT` séparé sans verrou
  ou contrainte — traiter ce pattern comme un signal d'alerte systématique en revue de code dès
  qu'une ressource partagée est en jeu (réservation, stock, siège, créneau).
- Privilégier la contrainte au niveau base quand c'est possible : elle protège même contre un bug
  applicatif futur qui oublierait le verrou, contrairement à une discipline purement côté code qui
  ne survit qu'aussi longtemps que personne ne l'oublie.
- Ajouter un test de concurrence explicite (plusieurs threads qui tentent la même réservation en
  parallèle) dans la suite de tests du module concerné — un test séquentiel classique ne détectera
  jamais ce genre de bug, peu importe sa couverture.

## 🔗 Liens

- [debugging-recipes/deadlock-postgres.md](../debugging-recipes/deadlock-postgres.md) — le
  verrouillage pessimiste mal ordonné peut introduire l'inverse du problème ici décrit
- `projects/macro-projects/hotel-booking-system` — le cas d'usage réel où ce bug est le plus
  évident (réservation de chambre par dates qui se chevauchent)
