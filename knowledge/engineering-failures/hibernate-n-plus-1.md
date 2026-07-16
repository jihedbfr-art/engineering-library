# N+1 (Hibernate)

> Charger une liste de N entités déclenche 1 requête pour la liste + N requêtes pour leurs relations
> lazy → la page qui marchait avec 10 lignes s'effondre à 10 000.

## 🔍 Cause
Une association `@OneToMany` / `@ManyToOne` en `FetchType.LAZY` est accédée dans une boucle. Hibernate
émet une requête SQL par élément parcouru au lieu d'une jointure. Le code semble innocent :
`notes.forEach(n -> n.getTags().size())`.

## 🚨 Symptômes
- Latence qui croît linéairement avec le nombre de lignes.
- Logs SQL noyés sous des `SELECT ... WHERE parent_id = ?` répétés.
- Souvent invisible en dev (peu de données), explosif en prod.

## 🩺 Comment diagnostiquer
Activer le compteur de requêtes et les stats Hibernate :

```properties
# application.properties (dev uniquement)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.SQL=DEBUG
```
Chercher dans les logs `getStatistics().getQueryExecutionCount()` >> nombre attendu, ou une même
requête paramétrée répétée N fois.

## ✅ Solution
Charger la relation en une requête : `JOIN FETCH` ou `@EntityGraph`.

```java
@Query("select n from Note n join fetch n.tags where n.owner = :owner")
List<Note> findAllWithTags(@Param("owner") String owner);

// ou, déclaratif :
@EntityGraph(attributePaths = "tags")
List<Note> findByOwner(String owner);
```
Pour de la pagination + collection, préférer `@BatchSize` (ex. 50) pour éviter le produit cartésien.

## 🛡️ Prévention
- Ne jamais itérer une relation lazy dans une boucle sans l'avoir fetchée.
- Un test qui compte les requêtes SQL (ex. `datasource-proxy` / `QuickPerf`) sur les endpoints listant.
- Voir [performance-recipes](../performance-recipes/) pour le tuning Hibernate.

## 🔗 Liens
- [engineering-decisions/0002-pourquoi-postgresql.md](../engineering-decisions/0002-pourquoi-postgresql.md)
- [code-review-guide](../code-review-guide/) — le N+1 est un smell à chercher en revue.
