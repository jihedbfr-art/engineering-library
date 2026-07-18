# Manuel utilisateur — LAN Monitor

Outil de supervision de **votre** réseau local. Tout fonctionne **uniquement pendant
que l'application est ouverte** ; rien ne tourne en arrière-plan une fois fermée.

---

## 1. Démarrer

1. Double-cliquez sur **`start.bat`** (ou en terminal : `python app.py`).
2. Ouvrez **http://127.0.0.1:8787** dans votre navigateur.
3. Connectez-vous avec **`admin` / `admin`**.
4. Changez le mot de passe dans l'onglet **Paramètres** dès la première utilisation.

> Astuce : lancer le terminal **en administrateur** donne une découverte réseau plus complète.

---

## 2. Les écrans

| Écran | À quoi il sert |
|---|---|
| **Vue d'ensemble** | Appareils en ligne, débit temps réel, graphique de bande passante, infos de connexion (IP locale, passerelle, IP publique). |
| **Appareils** | Tous les appareils du réseau : **type** (routeur, PC, téléphone…), IP privée, nom d'hôte, marque, MAC, **durée de connexion**, rôle. |
| **Trafic** | Historique de la bande passante, totaux et pics entrant / sortant. |
| **Débit** | Test de vitesse à la demande (téléchargement / envoi / ping / gigue). Bouton **Lancer le test**. |
| **Flux & destinations** | Connexions sortantes de cette machine, regroupées par **site / service** (Google, GitHub…), avec le temps passé sur chacun. |
| **Alertes** | Journal des événements de sécurité, filtrable par niveau, avec origine, explication et **conseils de défense**. |
| **Urgences** | Uniquement les menaces **critiques (rouge)** : attaques entrantes, scans de ports, accès non autorisés. |
| **Carte** | Topologie du réseau (tous les appareils autour du routeur) + point de sortie Internet géolocalisé. |
| **Aide** | Ce manuel, intégré dans l'application, et le bouton d'arrêt. |
| **Paramètres** | Changer le mot de passe admin. |

---

## 3. Comment arrêter l'application

Trois façons, au choix :

1. **Depuis l'interface** : onglet **Aide** → bouton rouge **« ⏻ Arrêter l'application »**.
   Le serveur se coupe proprement et la page affiche « Application arrêtée ».
2. **Dans le terminal** : appuyez sur **`Ctrl + C`**.
3. **Le plus simple** : **fermez la fenêtre noire** du terminal.

Après l'arrêt, plus rien ne tourne : aucune surveillance, aucun service résiduel.

---

## 4. Sécurité — ce que l'outil ne fait pas

- Il ne capte **pas** les identifiants, mots de passe ou la navigation des **autres**
  appareils : ce serait casser leur chiffrement HTTPS (interception illégale).
- Le bouton **Bloquer** ne coupe pas Internet par la force : il vous renvoie vers
  l'admin de votre **routeur** pour un blocage propre (filtrage MAC).
- Le mot de passe admin est stocké **haché** (avec sel) dans `data/config.json`,
  exclu de Git par `.gitignore`.
