# File Upload & Storage Manager

> Gestionnaire de fichiers et d'upload HTTP `multipart/form-data` sans aucune dépendance ni bibliothèque tierce. Développé en Java 17 natif (`com.sun.net.httpserver`) et Vanilla JS.

## Architecture & Choix Techniques

Ce projet fournit une solution complète de gestion de fichiers en mémoire et sur disque (Drag & Drop, upload, liste, téléchargement, suppression).

- **Backend** : Java 17 natif avec parser `multipart/form-data` développé sur-mesure (`Multipart.java`).
- **Sécurité & Contrôle** :
  - Validation explicite des limites de taille (10 Mo max).
  - Validation et désactivation des extensions d'exécutables dangereuses (`.exe`, `.bat`, `.sh`, `.cmd`, `.ps1`, `.vbs`, `.dll`).
  - Assainissement des noms de fichiers (sanitization anti Path-Traversal).
- **Frontend** : Interface utilisateur réactive avec Drag-and-Drop natif (HTML5 / CSS3 / Vanilla JS).

## Structure du Projet

```text
file-upload-manager/
├── src/
│   ├── Server.java      # Serveur HTTP REST & gestionnaire de stockage
│   ├── Multipart.java   # Parser multipart/form-data natif sans librairie
│   ├── Json.java        # Parser/Serializer JSON léger
│   └── HttpUtil.java    # Utilitaires HTTP (Body, Headers, MIME)
├── public/              # Interface Web (HTML, CSS, JS)
└── storage/             # Repertoire de stockage des fichiers uploades
```

## Démarrage Rapide

### Compiler et lancer

```bash
# Compilation des sources
javac -d bin src/*.java

# Exécution du serveur
java -cp bin Server
```

L'application est accessible sur **`http://localhost:8080`**.

## Endpoints API

| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/upload` | Upload un fichier (`multipart/form-data`) |
| `GET` | `/api/files` | Retourne la liste des fichiers stockés |
| `GET` | `/api/download` | Télécharge un fichier (`?name=filename.ext`) |
| `DELETE` | `/api/files` | Supprime un fichier (`?name=filename.ext`) |
