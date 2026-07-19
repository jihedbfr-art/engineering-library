# Pauses GC longues (JVM)

> L'application "gèle" quelques centaines de millisecondes à plusieurs secondes, de façon
> périodique et imprévisible du point de vue applicatif — le garbage collector fait le ménage,
> et pendant ce temps plus rien ne s'exécute.

## 🔍 Cause

Un GC "stop-the-world" (même avec les collecteurs modernes, certaines phases le restent) suspend
tous les threads applicatifs pendant qu'il libère la mémoire. La durée de la pause dépend
principalement de la quantité de mémoire vivante à parcourir/déplacer, pas seulement de la taille
du tas — un tas énorme mais avec peu d'objets vivants pause moins longtemps qu'un tas plus petit
saturé d'objets qui survivent. Les causes les plus fréquentes d'une dégradation progressive des
pauses : un heap sous-dimensionné par rapport au trafic réel (le GC travaille plus souvent, sur
plus de volume à chaque fois), une fuite mémoire lente qui remplit la old generation au fil du
temps, ou un pic de trafic qui génère un volume d'objets temporaires largement au-dessus de ce
que le dimensionnement initial avait prévu.

## 🚨 Symptômes

- Latence p99 avec des pics périodiques nets, alors que p50 reste stable — signature typique
  d'un GC qui n'affecte qu'une fraction des requêtes (celles qui tombent pendant la pause).
- Dans les logs GC (`-Xlog:gc*` depuis Java 9+), des pauses qui grandissent progressivement sur
  plusieurs heures/jours avant un redémarrage — signe d'une fuite plutôt que d'un
  sous-dimensionnement statique.
- CPU qui monte en flèche brièvement au moment de chaque pause (le GC utilise plusieurs threads),
  puis retombe — à ne pas confondre avec une charge applicative réelle.
- Dans les cas sévères : timeouts en cascade sur les appels amont pendant la pause (un service qui
  gèle 3 secondes fait timeout les appelants configurés avec un timeout plus court), transformant
  un problème de latence isolé en incident distribué.

## 🩺 Comment diagnostiquer

Activer les logs GC détaillés si ce n'est pas déjà fait :
```bash
-Xlog:gc*:file=/var/log/app/gc.log:time,level,tags:filecount=10,filesize=50M
```
Chercher la durée et la fréquence des pauses, et surtout leur tendance dans le temps :
```bash
grep "Pause" gc.log | tail -50
```
Si la old generation grandit sans jamais redescendre après un GC majeur sur plusieurs heures,
c'est une fuite mémoire, pas un problème de dimensionnement — dans ce cas, un heap dump
(`jmap -dump:live,format=b,file=heap.hprof <pid>`) analysé avec Eclipse MAT ou VisualVM identifie
généralement la classe qui accumule des instances sans jamais les libérer (cache sans éviction,
listener jamais désabonné, collection statique qui grossit indéfiniment sont les suspects
habituels).

## ✅ Solution

Selon ce que le diagnostic révèle :
- **Sous-dimensionnement simple** : augmenter `-Xmx`, ou passer à un collecteur plus adapté au
  profil de charge — G1GC (par défaut depuis Java 9) convient à la majorité des cas ; ZGC ou
  Shenandoah pour des exigences de pause quasi-nulle (sub-10ms) sur un très gros tas, au prix
  d'un débit un peu inférieur.
- **Fuite mémoire réelle** : corriger la source identifiée par le heap dump — pas de solution
  générique, ça dépend entièrement de ce qui accumule (voir diagnostic).
- **Trop de garbage généré par requête** : réduire les allocations inutiles dans le chemin chaud
  (objets temporaires créés en boucle, boxing/unboxing évitable, concaténation de String répétée
  au lieu de `StringBuilder`) — souvent plus efficace que de simplement grossir le tas, qui ne
  fait que retarder le problème.

```bash
# Point de depart raisonnable pour G1GC sur un service Spring Boot classique
-Xms2g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```
`-Xms` égal à `-Xmx` évite les pauses supplémentaires liées au redimensionnement du tas en cours
de route — un détail souvent oublié qui coûte cher au démarrage sous charge.

## 🛡️ Prévention

- Suivre la tendance de la old generation dans le temps (pas juste son état instantané) en
  monitoring — une croissance qui ne redescend jamais après GC majeur est le signal précoce d'une
  fuite, des semaines avant que ça devienne un incident visible.
- Dimensionner le heap sur la base d'un test de charge réaliste, pas sur une valeur par défaut ou
  copiée d'un autre service au profil différent.
- Passer en revue les caches applicatifs (locaux, pas Redis) pour s'assurer qu'ils ont une
  politique d'éviction — un `HashMap` statique utilisé comme cache "temporaire" qui ne l'a jamais
  eu revient régulièrement comme cause racine d'une fuite mémoire en production.

## 🔗 Liens
- [engineering-failures/redis-cache-stampede.md](redis-cache-stampede.md) — un autre mode de
  défaillance lié au cache, côté expiration cette fois
- [backend/java-spring/resilience4j-circuit-breaker.md](../backend/java-spring/resilience4j-circuit-breaker.md)
  — le `slow-call-duration-threshold` d'un circuit breaker est justement ce qui détecte qu'un
  appelant subit les pauses GC d'un dépendance en aval
