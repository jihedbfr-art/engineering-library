# Retry avec backoff : décider avant de répéter

[English version](README.md)

Cette petite démo Python retente une fonction avec un délai exponentiel auquel elle ajoute une part aléatoire. Elle permet d'étudier la boucle, puis les situations où répéter l'appel aggrave le problème. Elle utilise la bibliothèque standard et ne fait aucun appel réseau.

## Lancer la démo existante

Depuis la racine du dépôt :

```bash
cd projects/micro-projects/retry-with-backoff-cli
python retry_backoff.py
```

La fonction synthétique échoue deux fois avec ConnectionError, puis renvoie succes. Deux messages de retry précèdent la ligne finale succes. Les délais varient : environ 0,5–0,6 seconde, puis 1,0–1,1 seconde. Cinq tentatives correspondent à un appel initial et au plus quatre appels supplémentaires.

## Exercice : un timeout après une écriture

Une réponse absente ne dit pas à l'appelant si l'écriture a eu lieu. Prédisez le nombre d'entrées avant de lancer cette expérience locale depuis le même dossier :

```python
from contextlib import redirect_stdout
from io import StringIO
from unittest.mock import patch
from retry_backoff import retry_with_backoff

entries = []

def write_then_lose_response():
    entries.append("synthetic-order")
    raise TimeoutError("synthetic response lost")

with patch("retry_backoff.time.sleep"), redirect_stdout(StringIO()):
    try:
        retry_with_backoff(write_then_lose_response, max_attempts=2)
    except TimeoutError:
        pass

assert entries == ["synthetic-order", "synthetic-order"]
print(len(entries))
```

Enregistrez cet extrait dans un fichier d'exercice local non suivi et lancez-le avec Python, ou collez-le dans une session Python. Il affiche 2. Les écritures sont simulées par des ajouts à une liste ; aucune commande ne part vers un service. Simuler sleep supprime l'attente, sans supprimer les effets répétés. Cet exercice montre un risque de la boucle actuelle.

## Choisir l'action suivante

| Observation | Décision à justifier | Preuve nécessaire |
|---|---|---|
| Une lecture perd sa connexion | Envisager une nouvelle tentative dans le budget de temps | Contrat de l'opération, échéance restante, responsable des retries |
| La création d'une commande expire après envoi | Déterminer le résultat avant de réécrire, ou utiliser une déduplication documentée | Identité stable de l'opération et contrat serveur |
| La validation des données échoue | Corriger la requête | Détails de validation communicables à l'appelant |
| Un accès est refusé | Résoudre les droits selon le processus prévu | Décision d'autorisation ; un appel identique ne donne aucun droit |
| Le serveur demande de patienter | Appliquer le délai documenté si le budget restant le permet | Retry-After, si pertinent, et politique propre au service |
| Plusieurs couches retentent | Désigner le responsable et borner le travail total | Limites de tentatives à chaque couche |

À discuter : si trois couches font chacune trois tentatives au total, l'opération la plus profonde peut être appelée 27 fois pour une seule action initiale. C'est une borne arithmétique pour cette configuration, pas une mesure du script.

## Ce que l'implémentation actuelle ne couvre pas

La boucle intercepte toute Exception et retente donc aussi des échecs permanents. Elle ne gère ni timeout par appel, ni échéance globale, ni plafond de délai, ni Retry-After, ni stockage d'idempotence. Une fonction qui ne rend jamais la main bloque aussi la progression de la boucle.

Le jitter ajoute seulement 0–0,1 seconde au délai exponentiel ; il ne répartit pas le tirage sur toute la fenêtre du backoff. Le script affiche le texte des exceptions, qui peut contenir des données sensibles dans une intégration réelle. Utilisez ici des messages synthétiques. max_attempts et base_delay ne sont pas validés : zéro tentative renvoie silencieusement None.

Avant d'adapter ce code, définissez les erreurs qui autorisent un retry, validez la configuration, bornez le temps et les tentatives, puis précisez comment éviter les effets en double. Une clé d'idempotence exige un contrat serveur : périmètre, conservation, appels concurrents et requête modifiée. Ajouter un en-tête aléatoire ne suffit pas.

## Exercice de revue

Rédigez une politique pour une lecture et une création de commande. Précisez pour chacune le responsable des retries, les conditions d'arrêt, le résultat incertain après timeout et les observations possibles sans journaliser les identifiants ni les corps de requête. Distinguez les garanties du client de celles qui exigent le serveur.

## Sources

- [RFC 9110, section 9.2.2](https://www.rfc-editor.org/rfc/rfc9110.html#section-9.2.2) : idempotence des méthodes HTTP et limites des retries automatiques pour les requêtes non idempotentes.
- [AWS : contrôler et limiter les retries](https://docs.aws.amazon.com/wellarchitected/latest/framework/rel_mitigate_interaction_failure_limit_retries.html) : limites, backoff et prévention des effets en double.
- [AWS : rendre les retries sûrs avec des API idempotentes](https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/) : contrats de service pour les requêtes répétées.

Sources consultées le 15 octobre 2026. L'exercice caractérise la démo ; il ne démontre pas sa sûreté en production.
