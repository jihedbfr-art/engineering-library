# http-status-lookup-cli

Table des codes de statut HTTP (100-599, principaux codes) consultable par code exact ou
par mot-clé dans la description. Pratique pour vérifier rapidement le sens d'un code sans
quitter le terminal.

## Lancer

```bash
python http_status.py -c 404
python http_status.py -k "not found"
```

## Exemple d'usage

```bash
$ python http_status.py -c 418
418 I'm a Teapot

$ python http_status.py -k "timeout"
408 Request Timeout
504 Gateway Timeout
```

`-c/--code` et `-k/--keyword` sont mutuellement exclusifs. La recherche par mot-clé est
insensible à la casse.
