# XXE (XML External Entity)

## 🎯 Menace / objectif
Empêcher qu'un document XML fourni par un utilisateur fasse lire des fichiers arbitraires sur le serveur, exécute une requête SSRF depuis le serveur, ou provoque un déni de service — via la fonctionnalité d'entités externes du standard XML, activée par défaut dans beaucoup de parseurs.

## 🧠 Principe
La spécification XML permet de définir une entité qui référence une ressource externe (un fichier local, une URL) et de l'injecter dans le document au moment du parsing :
```xml
<?xml version="1.0"?>
<!DOCTYPE foo [ <!ENTITY xxe SYSTEM "file:///etc/passwd"> ]>
<foo>&xxe;</foo>
```
Si le parseur résout cette entité (comportement par défaut de beaucoup de parseurs Java/XML historiquement), le contenu du fichier référencé se retrouve littéralement injecté dans les données parsées — que l'application les affiche directement ou les traite en interne, le contenu du fichier système a fuité. La même mécanique permet de forcer le serveur à faire une requête HTTP vers une URL interne (`SYSTEM "http://internal-service/admin"`), transformant XXE en vecteur de SSRF (voir [ssrf.md](ssrf.md)).

## 🛠️ Mise en œuvre
La défense est presque toujours la même : désactiver le traitement des entités externes au niveau du parseur, pas essayer de filtrer le XML entrant (le filtrage par regex sur du XML est notoirement peu fiable).

```java
DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
factory.setXIncludeAware(false);
factory.setExpandEntityReferences(false);
```
Pour Spring qui utilise Jackson XML ou JAXB en interne pour désérialiser des payloads XML entrants, vérifier explicitement que le module de désérialisation utilisé applique bien ce durcissement — les valeurs par défaut varient selon la bibliothèque et sa version, et ça a changé plusieurs fois dans l'écosystème Java au fil des CVE découvertes.

## ❌ Erreurs classiques
- Désactiver DOCTYPE seulement (`disallow-doctype-decl`) en pensant que ça suffit, sans désactiver aussi les entités externes explicitement → selon le parseur, certaines variantes d'attaque n'utilisent pas de DOCTYPE classique.
- Se fier à une bibliothèque tierce pour parser du XML utilisateur sans vérifier sa configuration par défaut → beaucoup de parseurs XML historiques (avant que XXE devienne un point d'attention OWASP standard) ont des entités externes activées par défaut pour rester conformes au standard XML complet.
- Accepter du XML depuis une source utilisateur alors que JSON suffirait → chaque parseur XML supplémentaire dans une chaîne de traitement (upload de fichier, import SOAP, flux SVG) est une surface d'attaque XXE potentielle de plus à durcir individuellement.
- Oublier les formats qui embarquent du XML sans que ce soit évident : SVG, DOCX/XLSX (basés sur XML zippé), certains flux SOAP — un upload "image" au format SVG est un vecteur XXE classique souvent oublié parce qu'on ne pense pas "XML" en premier.

## ✅ Vérification
Envoyer un payload XXE de test pointant vers un fichier connu et inoffensif (ex: un fichier temporaire créé pour le test, jamais un vrai fichier système en environnement partagé) et vérifier que le parsing échoue ou que le contenu n'apparaît pas dans la réponse, plutôt que de vérifier positivement qu'"rien de mauvais ne s'est produit" — un test qui échoue proprement au parsing est le signal recherché.

## 🔗 Liens
- [ssrf.md](ssrf.md) — XXE est un des vecteurs qui mène à une SSRF depuis un document utilisateur
- OWASP XXE Prevention Cheat Sheet
