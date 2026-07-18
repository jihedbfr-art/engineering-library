# LAN Monitor

Tableau de bord de supervision de **votre** réseau local. Usage personnel.
100 % Python standard — **aucune dépendance à installer**, aucune connexion CDN.

## Lancer

Double-cliquez sur **`start.bat`**, ou en ligne de commande :

```
python app.py
```

Puis ouvrez **http://127.0.0.1:8787**

- Identifiants par défaut : **`admin` / `admin`**
- Changez le mot de passe dans l'onglet **Paramètres** (stocké haché avec sel dans `data/config.json`).

## Arrêter

Au choix : le bouton **« ⏻ Arrêter l'application »** dans l'onglet **Aide**, ou
**`Ctrl + C`** dans le terminal, ou fermez simplement la fenêtre. Rien ne subsiste
en arrière-plan après l'arrêt.

📖 Manuel complet : [`MANUEL.md`](MANUEL.md) (aussi intégré dans l'onglet **Aide** de l'app).

> Astuce : la découverte des appareils et le comptage d'octets sont plus complets
> si vous lancez le terminal **en administrateur** (ARP/ping/netstat sans restriction).

## Fonctionnalités

| Onglet | Contenu |
|---|---|
| **Vue d'ensemble** | Appareils en ligne, débit entrant/sortant temps réel, graphique bande passante, infos de votre connexion |
| **Appareils** | Tous les appareils du LAN : IP privée, nom d'hôte, constructeur (MAC), état en ligne, **durée de connexion**, dernière vue, rôle (passerelle / cette machine) |
| **Trafic** | Historique bande passante, totaux et pics entrant/sortant |
| **Débit** | Test de vitesse à la demande (téléchargement / envoi / ping / gigue) via Cloudflare |
| **Flux & destinations** | Connexions sortantes **de cette machine** regroupées par hôte distant, applications, ports et **temps cumulé** par destination |
| **Alertes** | Journal horodaté des événements de sécurité, filtrable par niveau (critique / avertissement / info), avec origine géolocalisée, explication et **conseils de défense** |
| **Urgences** | Vue dédiée aux menaces **critiques (rouge)** uniquement : attaques entrantes, scans de ports, accès non autorisés — avec bannière d'alarme et marche à suivre |
| **Carte** | IP publique géolocalisée (ville/pays/FAI) sur une carte schématique |
| **Paramètres** | Changement du mot de passe admin |

## Ce que l'outil ne fait pas (et pourquoi)

Il ne capte **pas** les mots de passe, identifiants, e-mails ou la navigation des
**autres** appareils du réseau. Aujourd'hui tout passe en HTTPS/TLS : récupérer ces
données exigerait une interception MITM (SSL-strip, faux certificats, ARP spoofing),
c'est-à-dire casser le chiffrement d'autrui — du vol d'identifiants et de l'écoute
illégale, même sur son propre réseau. Hors périmètre, volontairement.

De même, le bouton **Bloquer** n'utilise pas l'ARP spoofing : le blocage légitime
d'un appareil se fait via l'admin de votre **routeur** (filtrage MAC / contrôle
d'accès). Un point d'intégration API routeur est prévu dans `app.py` (`/api/block`).

## Comment ça marche (technique)

- **Découverte** : ping sweep du /24 (parallélisé) → lecture de la table `arp -a`.
- **Constructeur** : table OUI locale (préfixes MAC courants) dans `app.py`.
- **Trafic** : lecture périodique de `netstat -e` (octets interface), calcul du débit.
- **Flux** : `netstat -no` + `tasklist` → agrégation par hôte distant et temps cumulé.
- **Géo** : requête à `ip-api.com` sur l'IP publique de sortie (rafraîchie toutes les 10 min).

## Fichiers

```
app.py            serveur + collecte (pur stdlib)
web/              interface (login, dashboard, css, js — autonome)
data/             config.json (auth) + devices.json (historique appareils) — créés au 1er lancement
start.bat         lanceur Windows
```
