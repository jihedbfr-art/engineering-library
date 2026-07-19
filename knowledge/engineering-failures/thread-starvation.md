# Thread starvation (tous les threads du pool bloqués en attente)

> L'application devient totalement non-réactive — même les endpoints qui n'ont aucun lien avec le service lent en amont ne répondent plus — parce que tous les threads disponibles du pool HTTP sont occupés à attendre une réponse d'un appel synchrone qui ne revient jamais dans un délai raisonnable.

## 🔍 Cause

Un serveur d'application (Tomcat embarqué typiquement) sert les requêtes avec un pool de threads borné (200 par défaut sur Tomcat/Spring Boot classique). Si une requête fait un appel synchrone vers un service externe lent ou en panne, **sans timeout configuré**, le thread qui traite cette requête reste bloqué indéfiniment en attente de la réponse. Sous charge normale ça passe inaperçu ; mais si le service externe devient lent ou indisponible, chaque nouvelle requête qui tombe sur ce même chemin de code bloque un thread de plus — jusqu'à épuiser tout le pool. À ce moment-là, **même les requêtes qui n'ont rien à voir avec le service externe en panne** ne trouvent plus de thread disponible pour être traitées : une lenteur localisée sur un seul endpoint dégénère en panne totale de l'application.

## 🚨 Symptômes

- Timeout ou absence totale de réponse sur des endpoints qui n'appellent jamais le service externe en cause — c'est le signal le plus trompeur, l'équipe cherche souvent le problème du mauvais côté en premier parce que "ça n'a aucun rapport avec le service lent qu'on soupçonne".
- Métrique du pool de threads Tomcat (`tomcat.threads.busy` via Actuator/Micrometer) qui monte jusqu'au maximum configuré et y reste bloquée, plutôt que de fluctuer normalement avec le trafic.
- `jstack` sur le process montre un grand nombre de threads dans le même état `WAITING`/`TIMED_WAITING` avec exactement la même stack trace, tous bloqués au même point d'appel réseau.

## 🩺 Comment diagnostiquer

```bash
# 1. Vérifier immédiatement le pool de threads
curl localhost:8080/actuator/metrics/tomcat.threads.busy
curl localhost:8080/actuator/metrics/tomcat.threads.config.max

# 2. Thread dump pour voir où les threads sont réellement bloqués
jstack <pid> > threads.txt
grep -A 10 "WAITING" threads.txt | grep -B 2 "at java.net\|at okhttp3\|at org.apache.http" | sort | uniq -c | sort -rn
# les stack traces qui reviennent en très grand nombre identiques pointent
# directement le point d'appel bloquant en cause
```
Le thread dump est l'outil décisif ici : au lieu de deviner quel appel externe est en cause, il montre littéralement où chaque thread bloqué attend, avec le nom de la classe cliente (client HTTP, driver JDBC) qui donne immédiatement la piste.

## ✅ Solution

- **Timeout explicite sur tout appel réseau sortant**, sans exception — un client HTTP sans timeout de connexion et de lecture configuré attendra par défaut indéfiniment (ou avec un timeout système bien trop long pour être acceptable) :
```java
WebClient client = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(3))
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)))
        .build();
```
- **Circuit breaker** (Resilience4j) pour arrêter d'essayer d'appeler un service externe déjà identifié comme en panne, plutôt que de continuer à bloquer de nouveaux threads sur des appels voués à échouer — voir [backend/java-spring/resilience4j-circuit-breaker.md](../backend/java-spring/resilience4j-circuit-breaker.md) pour la configuration détaillée.
- **Isoler les threads par dépendance** (bulkhead pattern) : réserver un pool de threads séparé et borné pour les appels vers chaque dépendance externe critique, pour qu'une dépendance lente épuise au pire son propre pool réservé, jamais celui qui sert le reste de l'application.

## 🛡️ Prévention

- Checklist de revue de code systématique pour tout nouveau client HTTP/réseau ajouté : timeout de connexion configuré, timeout de lecture configuré, circuit breaker en place si l'appel est sur un chemin critique — aucun appel réseau sortant ne devrait passer en revue sans ces trois points vérifiés explicitement.
- Alerte sur `tomcat.threads.busy` approchant du maximum configuré, pas seulement sur le taux d'erreur HTTP — le pool qui se remplit est le signal précoce, le taux d'erreur n'apparaît qu'une fois la starvation déjà installée.
- Charge de test qui simule explicitement une dépendance externe lente (pas juste en panne franche) pour vérifier que le système dégrade proprement (erreurs rapides via circuit breaker) plutôt que de starve silencieusement.

## 🔗 Liens
- [backend/java-spring/resilience4j-circuit-breaker.md](../backend/java-spring/resilience4j-circuit-breaker.md) — la configuration concrète du circuit breaker qui prévient ce mode de panne
- [connection-leak.md](connection-leak.md) — mode de défaillance voisin côté pool de connexions base de données plutôt que threads HTTP
