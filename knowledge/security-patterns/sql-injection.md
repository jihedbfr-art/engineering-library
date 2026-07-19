# Injection SQL

## 🎯 Menace / objectif
Empêcher qu'une entrée utilisateur modifie la structure d'une requête SQL au lieu de n'en être
qu'une donnée — un attaquant qui y parvient peut lire des données hors de sa portée, contourner
l'authentification, ou modifier/supprimer des données arbitraires.

## 🧠 Principe
Le problème naît dès qu'une requête est construite par concaténation de chaînes incluant une
entrée utilisateur : le moteur SQL ne peut alors plus distinguer "ceci est une donnée" de "ceci
fait partie de la requête". La défense n'est pas d'échapper les caractères dangereux (fragile,
toujours un cas oublié) mais de **séparer structurellement** la requête de ses paramètres via des
requêtes préparées — le moteur SQL reçoit alors la requête et les valeurs séparément, il n'y a
plus d'ambiguïté possible peu importe ce que contient la valeur.

## 🛠️ Mise en œuvre
```java
// VULNERABLE : l'entree utilisateur devient litteralement du SQL
String query = "SELECT * FROM users WHERE username = '" + username + "'";
// un attaquant envoie : ' OR '1'='1
// requete resultante : SELECT * FROM users WHERE username = '' OR '1'='1'
// -> retourne TOUS les utilisateurs, contourne completement le filtre

// SUR : requete preparee, le parametre reste une donnee quoi qu'il contienne
PreparedStatement stmt = connection.prepareStatement(
    "SELECT * FROM users WHERE username = ?");
stmt.setString(1, username);
```
Avec Spring Data JPA, les méthodes dérivées et `@Query` avec paramètres nommés sont sûres par
construction :
```java
// SUR : parametre lie, pas de concatenation
@Query("SELECT u FROM User u WHERE u.username = :username")
Optional<User> findByUsername(@Param("username") String username);

// VULNERABLE malgre l'usage de JPA : concatenation dans un @Query natif
@Query(value = "SELECT * FROM users WHERE username = '" + "#{#username}" + "'", nativeQuery = true)
```
Le piège n'est pas l'ORM en tant que tel, c'est le moment où on en sort (SQL natif, `Criteria`
mal utilisé, ou un ORM différent) sans réappliquer la même discipline.

## ❌ Erreurs classiques
- Croire qu'un ORM protège automatiquement partout — un `@Query(nativeQuery = true)` avec
  concaténation de chaîne réintroduit exactement le même problème que du JDBC brut.
- Échapper les apostrophes "à la main" au lieu d'utiliser des requêtes préparées — une défense
  ad hoc oublie toujours un cas (encodage, caractères multi-octets, contexte différent comme un
  `LIKE` ou un `ORDER BY` dynamique).
- Construire dynamiquement un nom de colonne ou de table à partir d'une entrée utilisateur — les
  requêtes préparées protègent les **valeurs**, pas les identifiants SQL (noms de colonnes/tables) ;
  un nom de colonne dynamique doit être validé contre une whitelist stricte, jamais interpolé
  directement.
- Limiter la vigilance à SQL : la même logique s'applique à toute requête construite par
  concaténation — NoSQL (opérateurs MongoDB injectés via un payload JSON), LDAP, ou une commande
  shell construite à partir d'une entrée utilisateur.

## ✅ Vérification
Tenter une charge utile d'injection classique (`' OR '1'='1`, `'; DROP TABLE users; --`) dans
chaque champ de saisie qui alimente une requête, et vérifier qu'elle est traitée comme une simple
chaîne de caractères sans effet sur la requête exécutée — un test automatisé avec ces payloads sur
les endpoints qui acceptent une entrée texte libre est plus fiable qu'une vérification manuelle
ponctuelle.

## 🔗 Liens
- [devsecops/security/owasp-top10.md](../devsecops/security/owasp-top10.md) — vue d'ensemble
  OWASP (A03 — Injection), ce fichier en est le développement pour le cas SQL spécifiquement
- [xss.md](xss.md) — même famille de problème (mélange donnée/code), côté navigateur cette fois
