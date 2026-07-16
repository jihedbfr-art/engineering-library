# Idempotence des requêtes POST/PUT

## 🎯 Principe
Un client qui rejoue une même requête (timeout réseau, retry automatique) ne doit jamais produire
un effet dupliqué côté serveur.

## ✅ Bonne pratique
Pour une opération de création sensible à la duplication (paiement, commande, réservation), exiger
une clé d'idempotence fournie par le client et stockée côté serveur :
```http
POST /reservations
Idempotency-Key: 3f29a1c4-...
```
Le serveur vérifie si cette clé a déjà été traitée ; si oui, il renvoie la réponse déjà produite
sans réexécuter l'opération.

## ❌ Contre-exemple
```http
POST /reservations
```
sans aucune clé d'idempotence : un retry client après un timeout réseau (l'opération a en fait
réussi côté serveur, mais la réponse s'est perdue) crée une seconde réservation identique — bug
classique en environnement mobile/réseau instable.

## 💡 Exemple concret
`PUT` est nativement idempotent par définition HTTP (remplacer une ressource par le même contenu
plusieurs fois produit le même état final) — pas besoin de clé supplémentaire. `POST` ne l'est pas
par nature et nécessite ce mécanisme explicite dès que l'opération a un effet de bord qu'on ne veut
surtout pas dupliquer — typiquement une logique de cycle de vie d'entité (activation, suppression
logique, restauration) où une double exécution accidentelle aurait un impact métier réel.
