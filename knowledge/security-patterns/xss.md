# XSS (Cross-Site Scripting)

## 🎯 Menace / objectif
Empêcher qu'une entrée utilisateur non fiable soit interprétée comme du HTML/JavaScript par le
navigateur d'une autre victime — un script injecté qui s'exécute dans le contexte de
l'application peut voler des cookies de session, effectuer des actions au nom de l'utilisateur,
ou exfiltrer des données affichées à l'écran.

## 🧠 Principe
Même famille de problème que [l'injection SQL](sql-injection.md) : une donnée utilisateur qui se
retrouve interprétée comme du code au lieu de rester une donnée. Ici le "moteur" qui interprète le
code est le navigateur, pas une base SQL. Trois variantes distinctes :
- **Stocké** : le script malveillant est enregistré côté serveur (un commentaire, un champ de
  profil) et s'exécute pour **chaque** visiteur qui affiche la donnée — le plus dangereux, portée
  la plus large.
- **Réfléchi** : le script fait partie de la requête elle-même (paramètre d'URL renvoyé tel quel
  dans la réponse) — nécessite de piéger la victime pour cliquer un lien forgé.
- **DOM-based** : la vulnérabilité ne touche jamais le serveur, le JavaScript côté client insère
  lui-même une donnée non fiable dans le DOM de façon dangereuse (`innerHTML` avec une valeur
  venant de l'URL, par exemple).

## 🛠️ Mise en œuvre
La défense principale : échapper systématiquement à l'affichage, jamais faire confiance au fait
que "cette donnée a déjà été validée en entrée" — la validation en entrée et l'échappement en
sortie sont deux couches différentes, l'une ne remplace pas l'autre.

```html
<!-- VULNERABLE : injection directe dans le DOM -->
<div id="comment"></div>
<script>
document.getElementById("comment").innerHTML = userComment;  // execute tout <script> injecte
</script>

<!-- SUR : le texte reste du texte, jamais interprete comme du HTML -->
<script>
document.getElementById("comment").textContent = userComment;
</script>
```
Avec Thymeleaf (déjà utilisé dans les projets `standard-projects` de cette bibliothèque),
l'échappement est **automatique par défaut** avec `th:text` — le piège est `th:utext`
(unescaped), à réserver aux cas où le HTML est explicitement voulu et vient d'une source de
confiance :
```html
<!-- SUR par defaut : Thymeleaf echappe automatiquement -->
<p th:text="${comment.body}">...</p>

<!-- DANGEREUX si comment.body vient d'un utilisateur : rendu HTML brut, pas d'echappement -->
<p th:utext="${comment.body}">...</p>
```
Pour le contenu réellement destiné à contenir du HTML riche (un éditeur WYSIWYG), passer par une
librairie de sanitization dédiée (OWASP Java HTML Sanitizer côté serveur, DOMPurify côté client)
plutôt que d'afficher le HTML brut tel quel.

## ❌ Erreurs classiques
- Compter uniquement sur la validation en entrée ("on filtre les balises `<script>` à la saisie")
  — un filtre de validation se contourne presque toujours (encodage, casse, balises alternatives),
  et il protège seulement ce point d'entrée précis, pas tous les endroits où la donnée est ensuite
  affichée.
- Utiliser `innerHTML`/`th:utext`/équivalent par habitude ou par facilité, sans se demander si le
  contenu affiché contient vraiment du HTML de confiance.
- Oublier le Content-Security-Policy comme filet de sécurité complémentaire — même avec un
  échappement correct partout, un CSP bien configuré limite la casse d'un oubli en empêchant
  l'exécution de script inline non autorisé.

```
Content-Security-Policy: default-src 'self'; script-src 'self'; object-src 'none'
```

## ✅ Vérification
Injecter une charge utile classique (`<script>alert(document.cookie)</script>`, ou une variante
qui contourne un filtre naïf comme `<img src=x onerror=alert(1)>`) dans chaque champ affiché
ensuite à un autre utilisateur, et vérifier qu'elle s'affiche comme texte littéral plutôt que de
s'exécuter. Vérifier aussi que le header CSP est bien présent sur les réponses HTML.

## 🔗 Liens
- [sql-injection.md](sql-injection.md) — même famille de problème, côté base de données
- [devsecops/security/owasp-top10.md](../devsecops/security/owasp-top10.md) — XSS relève de la
  même catégorie A03 (Injection) dans la classification OWASP
