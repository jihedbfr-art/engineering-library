# Pagination

## 🎯 Principe
Ne jamais renvoyer une collection complète non bornée sur un endpoint de liste : toujours paginer,
même quand le volume actuel semble petit.

## ✅ Bonne pratique
Pagination par offset pour les cas simples (listes triées, volumes modérés) :
```http
GET /notes?page=0&size=20&sort=createdAt,desc
```
Réponse enveloppée avec les métadonnées de pagination (total d'éléments, nombre de pages) plutôt
qu'un simple tableau brut, pour que le client puisse construire une navigation sans requête
supplémentaire.

## ❌ Contre-exemple
```http
GET /notes
```
sans aucun paramètre de pagination : fonctionne en développement avec 10 lignes, tombe en
production quand la table atteint des dizaines de milliers de lignes — le endpoint charge tout en
mémoire côté serveur et sature la bande passante côté client.

## 💡 Exemple concret
Dans `projects/notes-app-microservices`, le endpoint de liste des notes utilise Spring Data JPA
`Pageable` directement en paramètre de contrôleur, ce qui gère offset/limite/tri sans code
supplémentaire :
```java
@GetMapping
Page<NoteDto> list(@ParameterObject Pageable pageable) {
    return noteRepository.findAll(pageable).map(NoteDto::from);
}
```
Pour un volume très important avec insertions fréquentes (le décalage d'offset devient incohérent
si des lignes sont insérées entre deux pages), préférer une pagination par curseur (keyset) basée
sur le dernier identifiant vu plutôt que sur un numéro de page.
