# Embeddings — choisir un modèle et une dimension

## 🎯 Objectif
Choisir un modèle d'embedding et une dimension vectorielle adaptés au volume et au besoin réel,
plutôt que de prendre par défaut le plus grand modèle disponible.

## 🧩 Contexte d'usage
Mise en place ou évolution d'un pipeline RAG (cf. `04-RAG`) : le choix du modèle d'embedding
détermine la dimension stockée (donc la taille de l'index PGVector), le coût de calcul, et la
qualité de la recherche sémantique.

## 🛠️ Recette
- **Dimension plus grande ≠ toujours meilleure recherche** : une dimension plus élevée capture plus
  de nuance mais coûte plus cher en stockage et en calcul de similarité — au-delà d'un certain
  point, le gain de pertinence devient marginal pour un usage métier classique (documentation,
  support, FAQ interne).
- **Cohérence obligatoire** : tous les vecteurs d'un même index doivent venir du même modèle
  d'embedding — changer de modèle en cours de route sans réindexer intégralement rend les
  comparaisons de similarité incohérentes (mélange de deux espaces vectoriels différents).
- **Mesurer avant de choisir** : constituer un petit jeu de questions/réponses représentatif du
  domaine et comparer 2-3 modèles candidats sur la pertinence des passages récupérés (cf.
  `10-Evaluation`) plutôt que de se fier au classement générique du modèle.

```text
Checklist avant de figer un choix :
[ ] Volume de documents actuel et projeté à 12 mois
[ ] Dimension du vecteur = coût de stockage par document × volume
[ ] Test de pertinence sur 20-30 questions réelles du domaine
[ ] Plan de réindexation si le modèle change plus tard
```

## ✅ Résultat attendu
Un choix de modèle d'embedding documenté et mesuré sur le domaine réel, avec un plan clair pour la
réindexation si un meilleur modèle apparaît plus tard.

## ⚠️ Piège
- **Mélanger des vecteurs de modèles différents dans le même index** sans s'en rendre compte après
  une migration partielle — la recherche renvoie alors des résultats incohérents sans erreur
  visible.
- **Ignorer le coût de réindexation** : changer de modèle d'embedding implique de recalculer tous
  les vecteurs existants, pas seulement les nouveaux — un coût à anticiper, pas à découvrir après
  coup.
