# Event Scheduler & Slot Booking System

> Serveur d'agenda et de réservation de créneaux horaires sans dépendance externe. Implémenté en Java natif (`com.sun.net.httpserver`) et Vanilla JS.

## Architecture & Choix Techniques

Ce projet illustre la construction d'une API REST légère et thread-safe pour la gestion de créneaux et la réservation avec prévention stricte du sur-booking.

- **Backend** : Java 17 natif sans framework (aucun jar externe, `HttpServer` JDK).
- **Stockage** : Fichier JSON (`data/slots.json`) manipulé via un parser JSON fait main (`Json.java`), avec synchronisation thread-safe (`ReentrantLock` dans `Store.java`).
- **Frontend** : Vanilla JS / HTML5 / CSS3 responsive sans build ni transpileur.
- **Règles métier** :
  - Un créneau possède une date, une heure et une capacité maximale.
  - La réservation est atomique : tentative de réservation simultanée gérée par verrouillage explicite.
  - Annulation / Suppression possible uniquement si aucun utilisateur n'a réservé le créneau.

## Structure du Projet

```text
event-scheduler-booking/
├── src/
│   ├── Server.java      # Point d'entrée HTTP (API + Fichiers statiques)
│   ├── Store.java       # Gestionnaire mémoire & persistance JSON (Thread-safe)
│   ├── Json.java        # Parser/Serializer JSON natif
│   └── HttpUtil.java    # Utilitaire d'échange HTTP & headers
├── public/              # Interface Web (HTML, CSS, JS)
└── data/                # Persistence des données (slots.json)
```

## Démarrage Rapide

### Compiler et lancer le serveur

```bash
# Compilation des sources Java
javac -d bin src/*.java

# Lancement du serveur
java -cp bin Server
```

Le serveur démarre sur **`http://localhost:8080`**.

## Endpoints API

| Méthode | Endpoint | Description | Query / Body |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/slots` | Liste les créneaux | `?date=2026-08-10` (optionnel) |
| `POST` | `/api/slots` | Crée un nouveau créneau | `{ "date": "2026-08-10", "time": "14:00", "capacity": 1 }` |
| `DELETE` | `/api/slots` | Supprime un créneau libre | `?id=<slot-id>` |
| `POST` | `/api/book` | Réserve un créneau | `{ "slotId": "...", "name": "...", "email": "..." }` |
