# Détecter un N+1 en revue de code

## 🔎 Quoi chercher
Une boucle qui accède à une relation JPA lazy (`@OneToMany`, `@ManyToOne`) élément par élément, ou
un appel `findById`/`findByX` exécuté à l'intérieur d'une boucle sur une collection déjà chargée.

## 💥 Pourquoi ça compte
Une seule requête initiale suivie de N requêtes supplémentaires (une par élément de la collection)
passe inaperçue en développement avec peu de données, et dégrade fortement la performance en
production avec un volume réel (cf.
[engineering-failures/hibernate-n-plus-1.md](../engineering-failures/hibernate-n-plus-1.md)).

## ❌ À rejeter
```java
List<Author> authors = authorRepository.findAll();
for (Author author : authors) {
    // Chaque appel déclenche une requête SQL séparée pour charger les livres de cet auteur
    List<Book> books = author.getBooks();
}
```

## ✅ Accepté
```java
// Une seule requête, jointure explicite sur la relation nécessaire
@Query("SELECT a FROM Author a JOIN FETCH a.books")
List<Author> findAllWithBooks();
```
