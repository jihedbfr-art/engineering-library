# prime-number-checker-cli

Vérificateur de primalité et générateur de premiers via crible d'Ératosthène.

- **Stack** : Python 3 stdlib (`math`). Aucune dépendance.
- **Lancer** : `python prime_checker.py 97` (test unique) ou `python prime_checker.py 50 --list` (crible jusqu'à N).
- **Tester rapidement** : `python prime_checker.py 97` doit confirmer que 97 est premier (exit 0), `100` doit infirmer (exit 1).
- **Fichier clé** : `prime_checker.py` (script unique, `is_prime` / `sieve_of_eratosthenes`).
- **Points d'attention** : `is_prime` teste jusqu'à `sqrt(n)` (pas de boucle jusqu'à n). Le crible est en O(n log log n), adapté à des N raisonnables (jusqu'à quelques millions).
