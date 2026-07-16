# age-calculator-cli

Calcul d'âge exact (années/mois/jours) avec gestion des années bissextiles.

- **Stack** : Python 3 stdlib (`datetime`, `calendar`, `argparse`). Aucune dépendance.
- **Lancer** : `python age_calculator.py 1990-06-15 --on 2026-07-16`
- **Tester rapidement** : `python age_calculator.py 2000-02-29 --on 2026-03-01` doit gérer le
  29 février correctement (26 ans, 0 mois, 1 jour).
- **Fichier clé** : `age_calculator.py`, fonction `compute_age` (emprunt de jours via
  `calendar.monthrange` sur le mois précédent).
- **Points d'attention** : lève une erreur explicite si la date de naissance est postérieure
  à la date de référence. Format strict `AAAA-MM-JJ`.
