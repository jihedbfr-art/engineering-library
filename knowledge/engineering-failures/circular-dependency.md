# Circular dependency (beans Spring qui s'attendent mutuellement)

> L'application refuse de démarrer avec `BeanCurrentlyInCreationException: Error creating bean with name 'X': Requested bean is currently in creation` — deux (ou plus) beans Spring dépendent l'un de l'autre au moment de la construction, et aucun des deux ne peut être créé en premier.

## 🔍 Cause

Spring construit les beans dans un ordre déterminé par leurs dépendances : pour instancier `ServiceA` qui a besoin de `ServiceB` dans son constructeur, Spring doit d'abord avoir `ServiceB` prêt. Si `ServiceB` a lui-même besoin de `ServiceA` dans son constructeur, aucun des deux ne peut être construit en premier — un cycle sans point d'entrée. C'est presque toujours le symptôme d'un vrai problème de conception : deux services qui se connaissent mutuellement au niveau construction indiquent généralement qu'une responsabilité est mal placée, qu'un troisième service devrait porter la logique partagée, ou qu'une des deux dépendances devrait être un événement plutôt qu'un appel direct.

## 🚨 Symptômes

- Échec au démarrage de l'application avec `BeanCurrentlyInCreationException`, la stack trace listant la chaîne complète des beans impliqués dans le cycle — l'information est là, juste dense à lire la première fois.
- Le problème n'apparaît parfois qu'après l'ajout d'une nouvelle dépendance à un service existant, alors que l'application démarrait sans souci avant — le cycle existait potentiellement de façon latente entre les deux couches, la nouvelle dépendance vient juste de le fermer.
- Cas plus insidieux : le cycle est "résolu" silencieusement grâce à l'injection par setter ou par champ (`@Autowired` sur un champ) au lieu de constructeur — l'application démarre, mais la conception reste tout aussi problématique, seul le symptôme de démarrage a disparu.

## 🩺 Comment diagnostiquer

```
# Le message d'erreur Spring liste explicitement la chaîne de dépendance en cycle
# Error creating bean with name 'orderService': Requested bean is currently in creation:
# Is there an unresolvable circular reference?
```
```java
// Activer un log plus détaillé du contexte au démarrage pour visualiser
// l'ordre de construction tenté avant l'échec
logging.level.org.springframework.beans.factory=DEBUG
```
La stack trace de l'exception contient déjà, dans l'ordre, la séquence exacte des beans que Spring a tenté de construire avant de détecter le cycle — c'est suffisant dans la grande majorité des cas pour identifier les deux (ou plus) classes en cause sans outil supplémentaire.

## ✅ Solution

Par ordre de préférence, du plus structurant au plus rapide :
- **Extraire la logique partagée dans un troisième service** dont dépendent les deux premiers, brisant le cycle en le transformant en dépendance descendante simple (A → C, B → C, plus de cycle).
- **Remplacer l'appel direct par un événement** (`ApplicationEventPublisher` en Spring, ou un vrai broker si le découplage doit traverser des services séparés) quand la relation entre A et B est en réalité "B doit réagir à ce que fait A", pas "B a besoin d'appeler A pour fonctionner" — voir [architecture-library/event-driven.md](../architecture-library/event-driven.md).
- **Injection par setter plutôt que par constructeur**, en dernier recours seulement, si le cycle est difficile à éliminer structurellement dans l'immédiat — Spring peut alors construire les deux beans partiellement puis résoudre les références après coup :
```java
@Service
class ServiceA {
    private ServiceB serviceB;
    @Autowired
    void setServiceB(ServiceB serviceB) { this.serviceB = serviceB; }
}
```
  Ce correctif fait taire l'exception au démarrage sans résoudre le vrai problème de conception sous-jacent — à traiter comme un pansement temporaire explicitement noté comme tel, pas comme la solution définitive.

## 🛡️ Prévention

- Préférer systématiquement l'injection par constructeur (pas par champ) — c'est justement parce que l'injection par constructeur échoue bruyamment au démarrage sur un cycle qu'elle le rend visible immédiatement, plutôt que de le masquer jusqu'à ce qu'il cause un bug plus difficile à tracer en production.
- Revue de conception dès qu'un nouveau service a besoin d'importer un autre service qui, directement ou indirectement, dépend déjà de lui — le cycle se voit souvent avant même d'écrire le code, en dessinant simplement le graphe de dépendances prévu.
- Garder les couches de dépendance dans un seul sens cohérent avec l'architecture choisie (voir [architecture-library](../architecture-library/README.md)) — un onion ou un clean architecture bien respecté rend un cycle entre couches structurellement impossible, pas juste rare.

## 🔗 Liens
- [architecture-library/event-driven.md](../architecture-library/event-driven.md) — la bonne solution quand le cycle vient d'une relation qui devrait être asynchrone
- [architecture-library/hexagonal.md](../architecture-library/hexagonal.md) — une frontière de port/adaptateur bien posée empêche structurellement ce genre de cycle entre couches
