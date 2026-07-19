# json-flatten-cli

Aplatit un JSON imbriqué en clés à plat séparées par des points (`b.c`, `b.d[0].e`).

## Lancer

```bash
echo '{"a":1,"b":{"c":2,"d":[1,2,{"e":3}]}}' | python flatten.py -
python flatten.py config.json
python flatten.py config.json --format env
```

## Exemple

```bash
$ echo '{"a":1,"b":{"c":"hello"}}' | python flatten.py - --format env
A=1
B_C=hello
```

Le format `env` sert surtout à repérer d'un coup d'œil quelles clés d'une config imbriquée
correspondraient à quelles variables d'environnement, pas à générer un `.env` prêt à l'emploi
tel quel (les valeurs non scalaires restent en JSON inline).
