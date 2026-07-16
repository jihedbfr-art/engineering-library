# `org.hibernate.LazyInitializationException: could not initialize proxy - no Session`

> L'exception classique quand on accède à une collection/relation lazy en dehors d'une session
> Hibernate active (typiquement en sérialisant une entité JPA directement en JSON).

## Causes probables (fréquentes → rares)
1. Une relation `@OneToMany`/`@ManyToOne` en `FetchType.LAZY` est accédée après la fin de la
   transaction (ex. dans le mapper JSON du contrôleur REST).
2. Le DTO de sortie expose directement l'entité JPA au lieu d'un DTO déjà entièrement chargé.
3. Une méthode annotée `@Transactional` a une portée trop courte par rapport à l'usage réel de
   l'entité en aval.

## Diagnostic pas-à-pas
```text
# 1. Repérer la stack trace : elle pointe vers le getter de la collection/relation lazy en cause
# 2. Vérifier où cette entité est utilisée : est-elle encore dans le contexte @Transactional
#    au moment de l'accès, ou déjà remontée jusqu'au contrôleur ?
# 3. Chercher un mapping direct entité → JSON (ex. retour direct de l'entité JPA par le contrôleur)
```

## Correctif
- Charger explicitement la relation nécessaire dans la requête (`JOIN FETCH` en JPQL, ou
  `@EntityGraph`) plutôt que de compter sur le lazy loading implicite hors session.
- Mapper vers un DTO (MapStruct, cf. stack du projet) **à l'intérieur** de la méthode
  `@Transactional`, jamais après son retour.

## Si ça ne suffit pas
Voir [engineering-failures/hibernate-n-plus-1.md](../engineering-failures/hibernate-n-plus-1.md) —
souvent la même cause racine (relations lazy mal maîtrisées) produit les deux symptômes selon le
contexte d'accès.
