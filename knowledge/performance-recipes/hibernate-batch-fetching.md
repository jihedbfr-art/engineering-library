# Hibernate : batch fetching pour réduire les allers-retours DB

> Le levier direct pour atténuer un N+1 quand la jointure explicite n'est pas praticable partout
> (relations multiples chargées à la demande selon le contexte d'affichage).

## 📏 Comment mesurer
Activer le log SQL (`show-sql` + compteur de requêtes en test d'intégration) et compter le nombre
de requêtes générées pour charger une collection d'entités avec leurs relations — un chiffre qui
grandit linéairement avec le nombre d'entités indique un N+1 (cf.
[engineering-failures/hibernate-n-plus-1.md](../engineering-failures/hibernate-n-plus-1.md)).

## 🎚️ Levier
```java
@Entity
class Author {
    @OneToMany(mappedBy = "author")
    @BatchSize(size = 20)  // regroupe les chargements lazy par lots de 20 au lieu d'un par un
    private List<Book> books;
}
```
Ou globalement via configuration :
```properties
spring.jpa.properties.hibernate.default_batch_fetch_size=20
```

## 📈 Gain attendu
Un chargement lazy de N entités passe de N requêtes individuelles à `ceil(N/20)` requêtes groupées
— un ordre de grandeur de réduction pour des collections de taille modérée à grande.

## ⚠️ Piège
Le batch fetching réduit le nombre de requêtes mais ne les élimine pas complètement comme le
ferait une jointure explicite (`JOIN FETCH`) — pour un accès systématique et prévisible à une
relation, la jointure explicite reste la solution la plus performante ; le batch fetching est un
filet de sécurité pour les accès moins prévisibles (chargement à la demande selon le contexte
d'affichage).
