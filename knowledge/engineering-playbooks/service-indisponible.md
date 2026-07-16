# Playbook — Service indisponible / healthcheck rouge

> Un microservice ne répond plus (healthcheck en échec, 5xx systématiques, ou absence totale de
> réponse), impactant les services qui en dépendent.

## 🚨 Déclencheur
Alerte de monitoring sur un healthcheck en échec, ou remontée d'un service consommateur qui ne
parvient plus à joindre le service concerné (timeouts OpenFeign/Eureka).

## ✅ Pré-requis
Accès aux logs du service concerné, à Eureka (registre de services) pour vérifier son état
d'enregistrement, et aux métriques d'infrastructure (CPU/mémoire/connexions) du conteneur.

## 📋 Étapes
1. Vérifier si le service est bien enregistré et visible dans Eureka — s'il en a été évincé,
   le problème est probablement une saturation de ressources (CPU/mémoire) plutôt qu'un bug
   applicatif isolé.
2. Consulter les derniers logs du service pour une exception explicite (erreur de connexion base,
   pool de connexions épuisé cf.
   [debugging-recipes/connexion-pool-epuise.md](../debugging-recipes/connexion-pool-epuise.md),
   OutOfMemoryError).
3. Si le service est simplement bloqué sans cause immédiatement diagnosticable, redémarrer
   l'instance (le conteneur) pour rétablir le service pendant que le diagnostic continue en
   parallèle — ne pas laisser un service en panne le temps de comprendre la cause racine.
4. Une fois le service rétabli, poursuivre le diagnostic sur les logs collectés avant redémarrage.

## 🔎 Vérification
Le healthcheck repasse au vert, les services consommateurs cessent de signaler des timeouts, et le
service réapparaît correctement enregistré dans Eureka.

## 📣 Communication
Prévenir les équipes des services consommateurs impactés dès la détection, et à nouveau une fois le
service rétabli.

## 📝 Après
Créer une entrée [engineering-failures](../engineering-failures/) si la cause racine révèle un
problème récurrent ou évitable (fuite de ressource, dimensionnement insuffisant).
