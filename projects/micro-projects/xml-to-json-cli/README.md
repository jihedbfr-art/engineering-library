# xml-to-json-cli

Convertit un fichier XML simple en JSON. Les attributs deviennent une clé `@attributes`,
le texte devient `#text` quand l'élément a aussi des attributs ou des enfants, et les
balises répétées deviennent des listes.

## Lancer

```bash
python xml_to_json.py data.xml
python xml_to_json.py data.xml -o data.json
```

## Exemple d'usage

```bash
$ cat data.xml
<person id="1"><name>Alice</name><age>30</age></person>

$ python xml_to_json.py data.xml
{
  "person": {
    "@attributes": {
      "id": "1"
    },
    "name": "Alice",
    "age": "30"
  }
}
```

Balises répétées au même niveau (ex: plusieurs `<item>`) -> tableau JSON.
