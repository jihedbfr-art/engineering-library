# sliding-window-log-cli

Mediane glissante (fenetre fixe, deque) sur une serie temporelle - utile pour lisser des metriques
bruitees (latence, CPU) sans etre sensible aux outliers comme la moyenne glissante.

## Lancer
```bash
python sliding_window_median.py 5 3 8 2 9 1 -w 3
```
