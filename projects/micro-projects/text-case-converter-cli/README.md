# text-case-converter-cli

Convertit une chaîne entre camelCase, snake_case, kebab-case et PascalCase, avec détection
automatique du format source (basée sur la présence de `_`, `-` ou de majuscules).

## Lancer

```bash
python case_converter.py "my_variable_name" --to camel
python case_converter.py "myVariableName" --to snake
python case_converter.py "HTTPServerName" --to kebab
```

## Exemple

```bash
$ python case_converter.py "my_variable_name" --to camel
Format detecte: snake_case
Resultat (camel): myVariableName
```
