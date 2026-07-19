# sentence-counter

Estime le nombre de phrases dans un texte en coupant sur `.`, `!`, `?`.

Lancer :

```
python main.py
```

Heuristique simple, pas un vrai parseur linguistique : une vraie abreviation non geree
(`etc.`, `ex.`) sera comptee comme une fin de phrase. Suffisant pour une estimation rapide,
pas pour un compte exact sur un texte a forte densite d'abreviations.
