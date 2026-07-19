# Spring Boot ne démarre pas

> `Application run failed` au démarrage, ou pire, le process se termine sans message clair — avant
> d'ouvrir un debugger, la stack trace complète (pas le résumé) donne presque toujours la réponse.

## Causes probables (fréquentes → rares)
1. **`BeanCreationException` / `NoSuchBeanDefinitionException`** : un bean requis n'existe pas
   (annotation `@Component`/`@Service` oubliée, package hors du scan `@ComponentScan`, ou
   dépendance manquante dans le classpath pour l'auto-configuration correspondante).
2. **Dépendance circulaire entre beans** (`A` dépend de `B` qui dépend de `A` via injection par
   constructeur) — Spring refuse de résoudre le cycle par défaut depuis Spring Boot 2.6.
3. **Port déjà utilisé** (`Web server failed to start. Port 8080 was already in use`) — une
   instance précédente de l'app tourne encore, ou un autre process occupe le port.
4. **Config manquante ou invalide** : une propriété requise par une auto-configuration
   (`spring.datasource.url`, `issuer-uri`...) absente ou mal formée fait échouer le contexte avant
   même d'arriver au code applicatif.
5. **Conflit de versions de dépendances** (`NoSuchMethodError`, `ClassNotFoundException` au
   démarrage) — deux dépendances transitives qui tirent des versions incompatibles d'une même
   librairie, symptôme classique après une montée de version partielle.

## Diagnostic pas-à-pas
```bash
# 1. Lire la stack trace COMPLETE, pas juste la premiere ligne "Application run failed"
#    Spring Boot affiche generalement une section "Action:" en bas qui donne la cause
#    et une suggestion de correctif directement lisible - c'est souvent suffisant a lui seul.

# 2. Bean manquant ou cycle : chercher specifiquement
grep -A5 "BeanCreationException\|Requested bean is currently in creation" app.log

# 3. Port occupe : identifier le process qui tient le port
lsof -i :8080          # macOS/Linux
netstat -ano | findstr :8080   # Windows

# 4. Conflit de dependances : voir l'arbre effectif et reperer les doublons de version
mvn dependency:tree | grep -B2 "conflict\|omitted for conflict"

# 5. Activer le debug logging Spring pour voir precisement ou le contexte echoue
java -jar app.jar --debug
```

## Correctif
Selon la cause identifiée à l'étape précédente :
- **Bean manquant** : vérifier l'annotation (`@Service`, `@Repository`, `@Component`) et que la
  classe se trouve bien dans un package scanné (sous le package de la classe `@SpringBootApplication`,
  ou explicitement inclus via `@ComponentScan(basePackages = ...)`).
- **Cycle entre beans** : casser le cycle en injectant l'un des deux via `@Lazy`, ou — mieux —
  revoir la conception, un vrai cycle de dépendance entre deux services est souvent le signe d'une
  frontière de responsabilité mal placée plutôt qu'un problème purement technique à contourner.
- **Port occupé** : tuer le process concurrent, ou changer `server.port` en dev.
- **Config manquante** : renseigner la propriété manquante, ou vérifier le profil actif
  (`spring.profiles.active`) — une propriété présente dans `application-prod.properties` mais
  absente en local est une source d'erreur fréquente au démarrage local.
- **Conflit de versions** : forcer la version cohérente via `<dependencyManagement>` (Maven) ou
  exclure la dépendance transitive problématique.

## Si ça ne suffit pas
Si l'application se termine sans aucune stack trace exploitable (process qui meurt silencieusement),
vérifier la mémoire disponible — un `OutOfMemoryError` au tout début du démarrage (chargement du
contexte Spring, souvent gourmand) peut tuer la JVM avant même que le logging applicatif soit
initialisé ; voir [engineering-failures/jvm-gc-pauses.md](../engineering-failures/jvm-gc-pauses.md)
pour le diagnostic mémoire plus large.
