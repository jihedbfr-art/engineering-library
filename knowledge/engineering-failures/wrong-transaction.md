# Wrong transaction (`@Transactional` qui ne s'applique pas, ou rollback qui ne se déclenche pas)

> Une méthode annotée `@Transactional` s'exécute, une exception est levée au milieu, et pourtant les écritures partielles restent en base — le rollback attendu ne s'est jamais produit, silencieusement.

## 🔍 Cause

Deux causes couvrent la grande majorité des cas réels, et aucune des deux ne lève d'erreur visible au moment où le problème se produit :

1. **Self-invocation** : `@Transactional` fonctionne via un proxy Spring généré autour du bean — quand une méthode de la même classe appelle une autre méthode transactionnelle **directement** (`this.autreMethode()` plutôt qu'en passant par le bean injecté), l'appel contourne complètement le proxy, donc aucune transaction n'est ouverte. C'est la cause la plus fréquente et la plus surprenante pour qui découvre Spring, parce que rien ne signale visuellement que l'annotation est ignorée.
2. **Rollback sur exception non couverte** : par défaut, Spring ne fait un rollback automatique que sur les `RuntimeException` (et `Error`) — une exception vérifiée (`checked exception`, qui hérite de `Exception` mais pas de `RuntimeException`) levée dans une méthode `@Transactional` **ne déclenche pas de rollback automatique** sauf si `rollbackFor` est explicitement configuré. Un développeur qui code une exception métier vérifiée en pensant "Spring va gérer le rollback comme d'habitude" se retrouve avec des écritures partielles commises malgré l'exception.

## 🚨 Symptômes

- Des enregistrements incomplets ou incohérents en base après une erreur applicative — une commande créée sans ses lignes associées, un virement débité sans être crédité de l'autre côté — alors que le code semble correctement annoté `@Transactional`.
- Aucune erreur au niveau de la transaction elle-même : la méthode s'exécute, l'exception est levée et propagée normalement, seul l'état final en base trahit l'absence de rollback.
- Le bug ne se manifeste souvent que sous un chemin d'erreur précis (une validation métier qui échoue à mi-parcours), pas sur le chemin nominal — ce qui le rend facile à manquer en test si les tests ne couvrent que le succès.

## 🩺 Comment diagnostiquer

```java
// Cas 1 — self-invocation : repérer un appel interne à une méthode @Transactional
@Service
class OrderService {
    @Transactional
    public void placeOrder(Order order) {
        save(order);
        this.sendConfirmation(order); // appel direct — ignore le proxy, sendConfirmation()
                                        // s'exécute SANS la protection transactionnelle attendue
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendConfirmation(Order order) { /* ... */ }
}
```
```java
// Cas 2 — vérifier le type d'exception levée dans une méthode @Transactional
class StockInsuffisantException extends Exception { /* checked — pas de rollback par défaut ! */ }
```
Activer le log Spring transaction pour voir concrètement quand une transaction s'ouvre et se ferme réellement, plutôt que de le déduire du code :
```
logging.level.org.springframework.transaction=DEBUG
```

## ✅ Solution

- **Self-invocation** : extraire la méthode appelée dans un bean séparé et l'injecter, ou s'auto-injecter le proxy (`@Autowired private OrderService self;` puis appeler `self.sendConfirmation(order)`) — la deuxième option fonctionne mais reste moins propre qu'une vraie séparation de responsabilité.
- **Rollback sur exception vérifiée** : déclarer explicitement `rollbackFor` si l'exception métier doit déclencher un rollback :
```java
@Transactional(rollbackFor = StockInsuffisantException.class)
public void placeOrder(Order order) throws StockInsuffisantException { /* ... */ }
```
  Ou, plus simple à retenir comme convention d'équipe : faire hériter systématiquement les exceptions métier de `RuntimeException` plutôt que de `Exception`, pour bénéficier du comportement de rollback par défaut sans avoir à y penser à chaque déclaration.

## 🛡️ Prévention

- Convention d'équipe explicite : les exceptions métier héritent de `RuntimeException`, jamais d'`Exception` — évite la classe entière de bugs liée au rollback silencieusement ignoré.
- Test d'intégration qui vérifie explicitement l'état de la base **après** une exception intentionnellement déclenchée en plein milieu d'une méthode transactionnelle, pas seulement le chemin de succès — c'est le seul type de test qui attrape ce bug avant la production.
- Revue de code attentive à tout appel `this.methode()` vers une autre méthode `@Transactional` de la même classe — un linter ou une règle ArchUnit peut détecter ce pattern automatiquement plutôt que compter sur la vigilance humaine à chaque revue.

## 🔗 Liens
- [connection-leak.md](connection-leak.md) — une transaction mal bornée (trop longue) retient une connexion, sujet voisin mais différent de ce fichier
- [debugging-recipes/deadlock-postgres.md](../debugging-recipes/deadlock-postgres.md) — autre catégorie de bug transactionnel, côté verrouillage plutôt que rollback
